package app.dao.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для преобразований всех строк в {@link ResultSet} в объекты указанного типа
 *      с помощью {@link RowMapper}.
 * @param <T> тип объекта
 */
public class ListRowMapper<T> {

    /**
     * Преобразовывает строки из {@link ResultSet} в объекты указанного типа с помощью {@link RowMapper}.
     * @param resultSet результат запроса
     * @param rowMapper преобразовывает 1 строку в объект
     * @return список получившихся объектов
     * @throws SQLException если происходит ошибка во время получения данных из {@link ResultSet}
     */
    public List<T> mapRowList(ResultSet resultSet, RowMapper<? extends T> rowMapper)
            throws SQLException {
        List<T> result = new ArrayList<>();
        while(resultSet.next()){
            result.add(rowMapper.mapRow(resultSet));
        }
        return result;
    }
}
