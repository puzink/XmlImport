package app.dao.rowmapper;

import app.table.Column;
import app.table.DataType;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Класс для преобразования строк {@link ResultSet} в {@link Column}.
 */
public class ColumnRowMapper implements RowMapper<Column> {

    /**
     * Преобразовывает строки из {@link ResultSet} в {@link Column}.
     * @param resultSet результат запроса
     * @return информация о колонке
     * @throws SQLException если произошла ошибка во время работы с {@link ResultSet}
     * @throws IllegalArgumentException если нет {@link DataType}, соответствующий sql-типу колонки
     */
    public Column mapRow(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString("column_name");
        String sqlType = resultSet.getString("data_type");
        DataType type = DataType.getBySqlType(sqlType)
                .orElseThrow(() -> new IllegalArgumentException("This data type is not supported: " + sqlType));
        return new Column(name, type);
    }

}
