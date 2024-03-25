package app.dao;

import app.table.Column;

import java.sql.SQLException;
import java.util.List;

/**
 * Класс выполняет запросы в СУБД, связанные с информацией о таблицах.
 */
public interface TableDao extends Dao {

    /**
     * Возвращает информацию о колонках таблицы.
     * @param tableName имя таблицы
     * @return набор столбцов
     * @throws SQLException если произошла ошибка во время выполнения запроса
     */
    List<Column> getTableColumns(String tableName) throws SQLException;

}
