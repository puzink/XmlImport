package app.repository;

import app.table.Column;
import app.table.Row;

import java.sql.SQLException;
import java.util.List;

public interface RowRepository extends Repository {

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
    int insertUniqueRows(List<Row> rows, List<Column> rowColumns,
                         List<Column> uniqueColumns, String tableName) throws SQLException;

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
    int insertRows(List<Row> rows, List<Column> rowColumns, String tableName) throws SQLException;


}
