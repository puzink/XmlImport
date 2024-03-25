package app.dao;

import app.table.Column;
import app.table.Row;

import java.sql.SQLException;
import java.util.List;

/**
 * Класс выполняет запросы в СУБД, связанные со строками таблиц.
 */
public interface RowDao extends Dao {

    /**
     * Вставляет строки по указанным столбцам в таблицу.
     * Если строку вставить нельзя: нарушает ограничение и т.д. -
     *      она пропускается(on conflict do nothing).
     * @param rows - строки
     * @param columns - столбцы, значения по которым будут вставлены
     * @param tableName - имя таблицы, в которую нужно вставить
     * @return кол-во вставленных строк
     * @throws SQLException - если произошла ошибка во время вставки строк
     */
    int insertRowsAsPossible(List<Row> rows, List<Column> columns, String tableName) throws SQLException;

    /**
     * Проверяет наличие дубликатов строк в таблице по указанным столбцам.
     * Две строки считаются равными, если у них совпадают значения по каждому из указанных столбцов:
     *      null-ы тоже сравниваются.
     * Например, {null,2} и {null,2} считаются равными.
     * @param rows - строки, у которых необъодимо проверить наличе дубликата
     * @param tableName - имя таблицы
     * @param uniqueColumns - столбцы, по которым будут сравниваться строки
     * @return список с результатом по каждой строке из полученных.
     *       Результат равен true, если у строки есть дубликат, иначе - false.
     * @throws SQLException - если произошла ошибка во время выполнения запроса
     */
    List<Boolean> hasDuplicateRow(List<Row> rows, String tableName, List<Column> uniqueColumns) throws SQLException;

}
