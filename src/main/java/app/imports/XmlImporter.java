package app.imports;

import app.imports.converter.StringConverter;
import app.imports.transaction.SerializationTransactionTask;
import app.repository.RowRepositoryImpl;
import app.repository.TableRepositoryImpl;
import app.table.Column;
import app.table.Row;
import app.table.Table;
import app.imports.converter.ConverterFactory;
import app.imports.transaction.ThreadConnectionTransactionManagerImpl;
import app.xml.Attribute;
import app.xml.Node;
import app.xml.XmlTableReader;
import lombok.Builder;
import lombok.Getter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Считывает строки таблицы из xml-файла и вставляет их.
 */
public class XmlImporter{

    private static final String UNIQUE_ATTRIBUTE = "unique";
    private static final String COLUMNS_ATTRIBUTE = "columns";
    private static final String NAME_ATTRIBUTE = "name";
    private static final String SEPARATOR = ";";

    private final RowRepositoryImpl repository;
    private final TableRepositoryImpl tableRepository;
    private final ThreadConnectionTransactionManagerImpl tx;
    private final Settings settings;

    public XmlImporter(RowRepositoryImpl repository,
                       TableRepositoryImpl tableRepository,
                       ThreadConnectionTransactionManagerImpl tx) {
        this(repository, tableRepository, tx, Settings.builder().build());
    }

    public XmlImporter(RowRepositoryImpl repository,
                       TableRepositoryImpl tableRepository,
                       ThreadConnectionTransactionManagerImpl tx,
                       Settings settings) {
        this.repository = repository;
        this.tableRepository = tableRepository;
        this.tx = tx;
        this.settings = settings;
    }

    /**
     * @see XmlImporter#importUniqueTableRows(XmlTableReader, Settings)
     */
    public long importUniqueTableRows(XmlTableReader tableReader) throws Exception {
        return importUniqueTableRows(tableReader, settings);
    }

    /**
     * Считывает строки и вставляет те из них, для которых нет дублирующих строк в таблице.
     * Вставка проиходит в многопоточном режиме с заданными настройками {@link Settings}
     *      без блокировки таблицы в БД,
     *      но с использованием транзакций с уровнем {@link Connection#TRANSACTION_SERIALIZABLE}.
     * Строки, которые не могут быть вставлены по каким-либо причинам, пропускаются.
     * Дубли строк определяются с помощью набора столбцов,
     *      который должен быть задан в узле({@link Node}), получаемым в {@link XmlTableReader#getTable()}.
     * Сравнение строк происходит через равенство('=') и проверку на null:
     *      строки {null} и {null} считаются равными.
     *
     * В конце работы происходит закрытие {@link XmlTableReader#close()}
     *      и прекращение работы executor-а {@link ExecutorService#shutdown()}.
     *
     * @param tableReader - считыватель строк из xml-файла
     * @param settings - настройки
     *
     * @return количество вставленных строк
     *
     * @throws IOException - если произошла ошибка во время чтения строк или таблицы,
     *          либо при закрытии {@link XmlTableReader#close}
     * @throws InterruptedException - если в конце работы алгоритма произошло зависание
     *      или поток был прерван из ожидания окончания работы executor-а:
     *      {@link #shutdownExecutorAndWaitCompletion(ExecutorService, long)}.
     * @throws XmlImportException - если неправильно заданы атрибуты таблицы
     * @throws SQLException - если невозможно соединиться с БД, указанной таблицы не существует
     *                      либо произошла ошибка во время вставки строк в БД
     * @throws IllegalArgumentException - если заданы неправильные настройки
     */
    public long importUniqueTableRows(XmlTableReader tableReader, Settings settings)
            throws Exception {
        ExecutorService executor = createExecutor(settings);
        ImportTableDto importTableDto = readTableInfo(tableReader);
        List<Row> rows = readRows(settings.readRowSize, importTableDto, tableReader);

        AtomicLong insertedRowsCount = new AtomicLong();
        long readRows = 0;
        while (!rows.isEmpty()) {
            readRows += rows.size();
            List<Row> convertedRows = convertRowsValues(rows, importTableDto.getTable().getColumns());

            runTaskForInsert(convertedRows, importTableDto, insertedRowsCount, executor);

            if(readRows % 1000 == 0){
                System.out.println("Read rows = " + readRows);
            }
            rows = readRows(settings.readRowSize, importTableDto, tableReader);
        }

        tableReader.close();
        shutdownExecutorAndWaitCompletion(executor, settings.timeToWaitExecutorCompleting);
        return insertedRowsCount.get();
    }

    /**
     * Создаёт executor с заданными настройками.
     * @param settings настройки
     * @return executor
     */
    private ExecutorService createExecutor(Settings settings) {
        return new BlockingExecutor(settings.threads, settings.threads,
                0, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(settings.taskQueueSize)
//                new ThreadPoolExecutor.CallerRunsPolicy()
        );

    }

    /**
     * Считывает табличный узел и определяет уникальные столбцы
     * и столбцы, по которым будут вставленны строки.
     * Также производит проверки на корректность столбцов:
     * 1) указанные столбцы должны быть в таблице,
     * 2) уникальные столбцы должны содержаться в столбцах для вставки.
     *
     * @param tableReader - поставщик данных о таблице
     * @return данные о таблице и столбцов, необходимых для проверки уникальности и вставки строк
     * @throws IOException - если произошла ошибка при чтении табличного узла в xml
     */
    private ImportTableDto readTableInfo(XmlTableReader tableReader)
            throws IOException, XmlImportException, SQLException {
        Node tableNode = tableReader.getTable();
        checkAttributesExistence(tableNode);
        String tableName = tableNode.getElement()
                .getAttributeBy(Attribute.filterByName(NAME_ATTRIBUTE))
                .get().getValue().trim();
        List<Column> tableColumns = tableRepository.getTableColumns(tableName);
        Table table = new Table(tableName, tableColumns);

        List<Column> uniqueColumns =
                splitAttributeValue(
                        tableNode.getElement()
                                .getAttributeBy(Attribute.filterByName(UNIQUE_ATTRIBUTE))
                                .get(),
                        SEPARATOR
                );
        List<Column> columnsForInsert =
                splitAttributeValue(
                        tableNode.getElement()
                                .getAttributeBy(Attribute.filterByName(COLUMNS_ATTRIBUTE))
                                .get(),
                        SEPARATOR
                );

        ImportTableDto importTableDto = new ImportTableDto(table, uniqueColumns, columnsForInsert);
        checkTableColumns(importTableDto);
        return importTableDto;
    }

    /**
     * Проверяет наличие имени таблицы и столбцов, по которым будут вставляться строки.
     *
     * @param tableNode - узел таблицы с атрибутами
     */
    private void checkAttributesExistence(Node tableNode) throws XmlImportException {
        List<Attribute> columnsAttribute =
                tableNode.getElement().getAttributesBy(Attribute.filterByName(COLUMNS_ATTRIBUTE));
        List<Attribute> tableName =
                tableNode.getElement().getAttributesBy(Attribute.filterByName(NAME_ATTRIBUTE));

        if (tableName.isEmpty()) {
            throw new XmlImportException("Table name is not defined.");
        }

        if (columnsAttribute.isEmpty()) {
            throw new XmlImportException("Column for insert is not defined.");
        }
    }

    /**
     * Проверяет корректность заданных атрибутов:
     *      каждый уникальный столбец({@link XmlImporter#UNIQUE_ATTRIBUTE})
     *      должен быть указан в столбцах для строк({@link XmlImporter#COLUMNS_ATTRIBUTE}),
     *      и каждый столбец строк должен быть в таблице.
     * Также наборы столбцов не должны содержать дублирующие столбцы.
     *
     * @param importTableDto - данные необходимые для импорта:
     *                       информация о таблице, уникальных столбцах и столбцах строк
     */
    private void checkTableColumns(ImportTableDto importTableDto) throws XmlImportException {

        Optional<Column> uniqueColumn =
                findNotExistedColumn(importTableDto.getUniqueColumns(), importTableDto.getColumnsForInsert());
        if (uniqueColumn.isPresent()) {
            throw new XmlImportException(
                    String.format(
                            "Unique column '%s' is not specified in the '%s' attribute.",
                            uniqueColumn.get().getName(),
                            COLUMNS_ATTRIBUTE
                    )
            );
        }
        Optional<Column> column =
                findNotExistedColumn(importTableDto.getColumnsForInsert(), importTableDto.getTable().getColumns());
        if (column.isPresent()) {
            throw new XmlImportException(
                    String.format(
                            "Column '%s' in the '%s' attribute is not specified in the table.",
                            column.get().getName(),
                            COLUMNS_ATTRIBUTE
                    )
            );
        }

        Optional<Column> duplicateUniqueColumn = findDuplicateColumn(importTableDto.getUniqueColumns());
        if (duplicateUniqueColumn.isPresent()) {
            throw new XmlImportException(
                    String.format("Attribute '%s' has a duplicate value '%s'.",
                            UNIQUE_ATTRIBUTE,
                            duplicateUniqueColumn.get().getName()
                    )
            );
        }

        Optional<Column> duplicateTableColumn = findDuplicateColumn(importTableDto.getColumnsForInsert());
        if (duplicateTableColumn.isPresent()) {
            throw new XmlImportException(
                    String.format("Attribute '%s' has a duplicate value '%s'.",
                            COLUMNS_ATTRIBUTE,
                            duplicateTableColumn.get().getName()
                    )
            );
        }
    }

    /**
     * Считывает указанное количество корректных строк.
     * Строка считается корректной, если столбцы строки совпадают с указанными в xml-файле.
     * Если нужно считать больше строк, чем есть, тогда считываются все оставшиеся строки.
     *
     * @param rowCount       - кол-во строк для чтения
     * @param importTableDto - данные о таблице и заданных в xml-файле столбцах
     * @param tableReader    - считыватель строк
     * @return список считанных и корректных строк
     * @throws IOException - если произошла ошибка при чтении строк
     */
    private List<Row> readRows(int rowCount, ImportTableDto importTableDto, XmlTableReader tableReader)
            throws IOException {
        Row row;
        List<Row> rows = new ArrayList<>();
        while (rows.size() < rowCount && (row = tableReader.readRow()) != null) {
            if (!Objects.equals(row.getValues().size(), importTableDto.getColumnsForInsert().size())) {
                continue;
            }
            if (!row.containsColumns(importTableDto.getColumnsForInsert())) {
                continue;
            }
            rows.add(row);
        }
        return rows;
    }

    /**
     * Преобразует строковые({@link String}) значения строк в типы,
     *      которые определяются из sql-типа столбца.
     * При конвертации создаются новые строки с измененными значениями, а не изменяются старые.
     *
     * @param rows         - строки, значения которых необходимо преобразовать
     * @param tableColumns - столбцы таблицы, для которых нужно преобразовать значения строк
     * @return новые строки с преобразованными значениями
     */
    private List<Row> convertRowsValues(List<Row> rows, List<Column> tableColumns) {
        List<Row> result = new ArrayList<>();
        for (Row row : rows) {
            try {
                Row convertedRow = convertRowValues(row, tableColumns);
                result.add(convertedRow);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return result;
    }

    /**
     * Преобразует строковые({@link String}) значения строки для выбранных столбцов.
     * Тип, в который преобразуется значение, определяется из sql-типа столбца.
     * При конвертации создаётся новая строка, а не изменяется старая.
     *
     * @param row          - строка
     * @param tableColumns - столбцы, для которых необходимо преобразовать значения
     * @return новая строка с преобразованными значениями
     * @see StringConverter
     * @see app.table.DataType
     */
    private Row convertRowValues(Row row, List<Column> tableColumns) {
        Map<String, Object> convertedValues = new HashMap<>();
        for (Column column : tableColumns) {
            if (row.containsColumn(column)) {
                String stringValue = (String) row.get(column);
                Object convertedValue = ConverterFactory.getFactory()
                        .getRightConverter(column.getType())
                        .convert(stringValue);
                convertedValues.put(column.getName(), convertedValue);
            }
        }
        return new Row(convertedValues);
    }

    /**
     * Находит столбцы с одиннаковыми именами.
     * @param columns - столбцы
     * @return столбец, который встречается более 1 раза. Иначе {@link Optional#empty()}
     */
    private Optional<Column> findDuplicateColumn(List<Column> columns) {
        Set<Column> columnSet = new HashSet<>();
        for (Column col : columns) {
            if (columnSet.contains(col)) {
                return Optional.of(col);
            }
            columnSet.add(col);
        }
        return Optional.empty();
    }

    /**
     * Ищет столбец из первого списка, который не содержится во втором.
     * Столбцы сравниваются через {@link Objects#equals(Object, Object)}
     * @param first  - список столбцов, каждый столбец которого должен находиться во втором списке
     * @param second - список столбцов, который содержит столбцы первого
     * @return столбец, которого есть в первом списке, но нет во втором. Иначе {@link Optional#empty()}
     */
    private Optional<Column> findNotExistedColumn(List<Column> first, List<Column> second) {
        for (Column colF : first) {
            if (!second.contains(colF)) {
                return Optional.of(colF);
            }
        }
        return Optional.empty();
    }

    /**
     * Создаёт столбцы по именам, заданных в значении атрибута и разделенных сепаратором.
     *
     * @param attribute - атрибут с именами столбцов
     * @param separator - разделелитель между именами столбцов
     * @return набор столбцов, созданных по именам
     */
    private List<Column> splitAttributeValue(Attribute attribute, String separator) {
        return Arrays.stream(attribute.getValue().split(separator, -1))
                .map(String::trim)
                .map(Column::new)
                .collect(Collectors.toList());
    }

    /**
     * Создаёт и запускает задачу с транзакцией для вставки уникальных строк в БД.
     * @param rows - строки, которые нужно вставить
     * @param importTableDto - информация о таблице, столбцах
     * @param insertedRowsCount - количетсво вставленных строк
     * @param executor
     */
    private void runTaskForInsert(List<Row> rows,
                                  ImportTableDto importTableDto,
                                  AtomicLong insertedRowsCount,
                                  ExecutorService executor) {

        Callable<Long> task = new SerializationTransactionTask<>(tx) {
            @Override
            public Long callTask() throws Exception {
                int res = repository.insertUniqueRows(
                        rows,
                        importTableDto.getColumnsForInsert(),
                        importTableDto.getUniqueColumns(),
                        importTableDto.getTable().getName()
                );
                return (long) res;
            }

            @Override
            public void afterCommit(Long taskResult) {
                insertedRowsCount.addAndGet(taskResult);
            }
        };

        executor.submit(task);
    }

    /**
     * Завершает работу executor-а и ожидает окончания выполнения всех задач.
     * @param executor
     * @param timeToWait время ожидания завершения работы executor-а
     * @throws InterruptedException если превышено время ожидания или ожидание прервано
     */
    private void shutdownExecutorAndWaitCompletion(ExecutorService executor, long timeToWait)
            throws InterruptedException {
        executor.shutdown();
        boolean isCompleted = executor.awaitTermination(timeToWait, TimeUnit.MILLISECONDS);
        if (!isCompleted) {
            throw new InterruptedException("Time of executor termination is exceeded.");
        }
    }


    /**
     * Настройки импортера.
     */
    @Builder
    @Getter
    public static class Settings{

        // Кол-во потоков, занимающихся вставкой строк в БД
        @Builder.Default
        private int threads = 1;

        /**
         * Время ожидания окончания работы потоков импортера после считывания всех строк из файла
         * @see #shutdownExecutorAndWaitCompletion(ExecutorService, long)
         */
        @Builder.Default
        private long timeToWaitExecutorCompleting = Long.MAX_VALUE;
        
        // Размер считваемых строк за раз
        @Builder.Default
        private int readRowSize = 100;

        // Размер очереди executor-а
        @Builder.Default
        private int taskQueueSize = 20;
    }
}
