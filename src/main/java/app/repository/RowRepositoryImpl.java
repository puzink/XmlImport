package app.repository;

import app.dao.RowDao;
import app.table.Column;
import app.table.Row;
import app.imports.transaction.ThreadConnectionPool;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RowRepositoryImpl extends AbstractRepository implements RowRepository {

    public final RowDao rowDao;

    public RowRepositoryImpl(ThreadConnectionPool connectionPool,
                             RowDao rowDao) {
        super(connectionPool);
        this.rowDao = rowDao;
    }

    /**
     * Вставляет уникальные строки по указанным столбцам в таблицу.
     * Уникальность строки проверяется сравнением значений по столбцам вплоть до null:
     *      строки {null, 2} и {null, 2} равны.
     * Если строку вставить нельзя: нарушает ограничение и т.д. -
     *      она пропускается(on conflict do nothing).
     * @param rows строки, которые необходимо вставить
     * @param rowColumns столбцы, по которым происходит вставка строк
     * @param uniqueColumns столбцы, по которым определяется уникальность строк
     * @param tableName имя таблицы
     * @return кол-во вставленных строк
     * @throws SQLException если произошла ошибка во время вставки строк
     */
    public int insertUniqueRows(List<Row> rows, List<Column> rowColumns,
                                List<Column> uniqueColumns, String tableName) throws SQLException {
        List<Row> uniqueRows = removeDuplicates(rows, tableName, uniqueColumns);
        return insertRows(uniqueRows, rowColumns, tableName);
    }

    /**
     * Вставляет строки по указанным столбцам в таблицу.
     * Если строку вставить нельзя: нарушает ограничение и т.д. -
     *      она пропускается(on conflict do nothing).
     * @param rows строки, которые необходимо вставить
     * @param rowColumns столбцы, по которым происходит вставка строк
     * @param tableName имя таблицы
     * @return кол-во вставленных строк
     * @throws SQLException если произошла ошибка во время вставки строк
     */
    public int insertRows(List<Row> rows, List<Column> rowColumns, String tableName) throws SQLException {
        return rowDao.insertRowsAsPossible(rows, rowColumns, tableName);
    }


    /**
     * Возвращает список строк, дубликатов которых нет в таблице и в самом списке.
     * Уникальность строки определяется сравнением значений
     *          по уникальным столбцам(по null тоже сравниваются).
     * @param rows - строки
     * @param tableName - имя таблицы
     * @param uniqueColumns - уникальные столбцы, по которым сравниваются строки
     * @return строки, уникальные по определённым столбцам
     */
    private List<Row> removeDuplicates(List<Row> rows, String tableName, List<Column> uniqueColumns)
            throws SQLException {
        if(uniqueColumns.isEmpty() || rows.isEmpty()){
            return rows;
        }

        List<Boolean> isRowDuplicateInTable =
                rowDao.hasDuplicateRow(rows, tableName, uniqueColumns);
        Set<Row> rowsToInsert = new HashSet<>();
        for(int i = 0; i<rows.size();++i){
            Row rowProjection = rows.get(i).projectOnto(uniqueColumns);
            if(!isRowDuplicateInTable.get(i)){
                rowsToInsert.add(rowProjection);
            }
        }

        return new ArrayList<>(rowsToInsert);
    }
}

