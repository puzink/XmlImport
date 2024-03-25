package app.imports.converter;

/**
 * Абстрактная реализация, которая возвращает null,
 *          если строка {@code str = null || str.equalsIgnoreCase("null")}.
 * @param <T> - тип, в который необходимо преобразовать строку.
 */
public abstract class NullableStringConverter<T> implements StringConverter<T> {

    /**
     * Преобразует строку в нужный тип.
     * Возвращает null, если строка {@code str = null или str.equalsIgnoreCase("null")}.
     * @param s - строка
     * @return преобразованный из строки объект.
     *          null - если {@code str = null или str.equalsIgnoreCase("null")}.
     */
    @Override
    public T convert(String s) {
        if(s == null || s.equalsIgnoreCase("null")){
            return null;
        }
        return convertNotNullString(s);
    }

    /**
     * Преобразует строку в нужный тип.
     * Строка не должна быть равняться null.
     * @param s - строка
     * @return преобразованный из строки объект
     */
    public abstract T convertNotNullString(String s);
}
