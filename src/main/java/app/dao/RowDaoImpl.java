package app.dao;

import app.table.Column;
import app.table.Row;
import app.utils.DbUtils;
import app.imports.transaction.ThreadConnectionPool;
import app.utils.QueryCreator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Обычная реализация {@link RowDao}.
 */
public class RowDaoImpl extends AbstractDao implements RowDao {

    public RowDaoImpl(ThreadConnectionPool connectionPool){
        super(connectionPool);
    }

    /**
     * Вставляет строки по указанным столбцам в таблицу.
     * Если строку вставить нельзя: нарушает ограничение и т.д. -
     *      она пропускается(on conflict do nothing).
     * @param rows строки
     * @param columns столбцы, значения по которым будут вставлены
     * @param tableName имя таблицы, в которую нужно вставить
     * @return кол-во вставленных строк
     * @throws SQLException если произошла ошибка во время вставки строк
     */
    public int insertRowsAsPossible(List<Row> rows, List<Column> columns, String tableName) throws SQLException {
        if(rows.isEmpty() || columns.isEmpty()){
            return 0;
        }

        String query = QueryCreator.insertRowStatement(tableName, columns, rows.size());
        query = query + " on conflict do nothing";

        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try{
            conn = getConnection();
            preparedStatement = conn.prepareStatement(query);
            for(int i = 0; i < rows.size();++i){
                for(int j = 0 ; j < columns.size();++j){
                    preparedStatement.setObject(
                            i * columns.size() + j + 1,
                            rows.get(i).get(columns.get(j))
                    );
                }
            }

            return preparedStatement.executeUpdate();
        } finally {
            DbUtils.closeQuietly(preparedStatement);
        }
    }


    /**
     * Проверяет наличие дубликатов строк в таблице по указанным столбцам.
     * У строки "row" есть дубликат, если есть такая строка "tableRow" в таблице,
     *     что для каждого столбца "col" из указанных справедливо выражение:<br>
     *     <i>(row.col = tableRow.col) or
     *     (((row.col is null::integer)) + ((tableRow.col is null)::integer) = 2)</i>.
     *
     * <p>В СУБД выполняется 1 запрос:<br>
     * {@link QueryCreator#hasDuplicateStatement(int rowsCount, List columns, String tableName)} </p>
     *
     * @param rows строки, у которых необъодимо проверить наличе дубликата
     * @param tableName имя таблицы
     * @param uniqueColumns столбцы, по которым будут сравниваться строки
     * @return список с результатом по каждой строке из полученных.
     *       <p>Результат равен true, если у строки есть дубликат, иначе - false.</p>
     * @throws SQLException если произошла ошибка во время выполнения запроса
     */
    public List<Boolean> hasDuplicateRow(List<Row> rows, String tableName, List<Column> uniqueColumns)
            throws SQLException {

        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try{
            conn = getConnection();
            String query = QueryCreator.hasDuplicateStatement(rows.size(), uniqueColumns, tableName);
            preparedStatement = conn.prepareStatement(query);
            int uniqueColumnSize = uniqueColumns.size();
            for(int i = 0; i<rows.size();++i){
                for(int j = 0; j < uniqueColumns.size();++j){
                    Column col = uniqueColumns.get(j);
                    preparedStatement.setObject(i*uniqueColumnSize + j + 1, rows.get(i).get(col));
                }
            }

            ResultSet resultSet = preparedStatement.executeQuery();
            List<Boolean> result = new ArrayList<>();
            while(resultSet.next()){
                result.add(resultSet.getBoolean(1));
            }
            return result;
        }finally {
            DbUtils.closeQuietly(preparedStatement);
        }
    }
}
