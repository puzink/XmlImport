package app.repository;

import app.dao.TableDao;
import app.table.Column;
import app.imports.transaction.ThreadConnectionPool;

import java.sql.SQLException;
import java.util.List;

public class TableRepositoryImpl extends AbstractRepository implements TableRepository {

    private final TableDao tableDao;

    public TableRepositoryImpl(ThreadConnectionPool connectionPool,
                               TableDao tableDao) {
        super(connectionPool);
        this.tableDao = tableDao;
    }

    /**
     * Возвращает информацию о колонках таблицы.
     * @param tableName имя таблицы
     * @return список столбцов
     * @throws SQLException если произошла ошибка во время выполнения запроса
     * @see Column
     */
    public List<Column> getTableColumns(String tableName) throws SQLException {
        return tableDao.getTableColumns(tableName);
    }
}
