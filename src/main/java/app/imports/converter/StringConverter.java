package app.imports.converter;

/**
 * Интерфейс для преобразования строк в объект необходимого типа.
 * @param <T> - тип, в который необходимо преобразовать строку.
 */
@FunctionalInterface
public interface StringConverter<T> {

    T convert(String s);

}
