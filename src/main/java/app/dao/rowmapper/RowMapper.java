package app.dao.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Класс для преобразования строк {@link ResultSet} в объект конкретного типа.
 * @param <T> тип, в который преобразуется строка
 */
@FunctionalInterface
public interface RowMapper<T> {

    /**
     * Преобразовывает строку в объект конкретного типа.
     * После считывания строки из {@link ResultSet} НЕ происходит переход к новой строке.
     * @param resultSet результат запроса
     * @return объект, созданный из значений {@link ResultSet}
     * @throws SQLException если произошла ошибка во время работы с {@link ResultSet}
     */
    T mapRow(ResultSet resultSet) throws SQLException;
}
