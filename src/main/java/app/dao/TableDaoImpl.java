package app.dao;

import app.dao.rowmapper.ColumnRowMapper;
import app.dao.rowmapper.ListRowMapper;
import app.table.Column;
import app.table.DataType;
import app.utils.DbUtils;
import app.imports.transaction.ThreadConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Обычная реализация {@link TableDao}.
 */
public class TableDaoImpl extends AbstractDao implements TableDao {

    public TableDaoImpl(ThreadConnectionPool connectionPool) {
        super(connectionPool);
    }

    /**
     * Возвращает информацию о колонках таблицы.
     * @param tableName имя таблицы
     * @return набор столбцов
     * @throws SQLException если произошла ошибка во время выполнения запроса
     * @throws IllegalArgumentException если нет {@link DataType}, соответствующий sql-типу колонок
     */
    public List<Column> getTableColumns(String tableName) throws SQLException {

        String query =
                "select column_name, data_type from information_schema.columns where table_name = ?";
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try{
            conn = getConnection();
            preparedStatement = conn.prepareStatement(query);
            List<Column> columns = new ArrayList<>();
            preparedStatement.setString(1, tableName);
            ResultSet resultSet = preparedStatement.executeQuery();
            return new ListRowMapper<Column>().mapRowList(resultSet, new ColumnRowMapper());
        } finally {
            DbUtils.closeQuietly(preparedStatement);
        }
    }

}
