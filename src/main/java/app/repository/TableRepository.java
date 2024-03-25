package app.repository;

import app.table.Column;

import java.sql.SQLException;
import java.util.List;

public interface TableRepository extends Repository{

    /**
     * Возвращает информацию о колонках таблицы.
     * @param tableName имя таблицы
     * @return список столбцов
     * @throws SQLException если произошла ошибка во время выполнения запроса
     * @see Column
     */
    List<Column> getTableColumns(String tableName) throws SQLException;
}
