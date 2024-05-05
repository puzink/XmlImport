package app.imports.converter;

/**
 * Конвертер-пустышка, который ничего не делает со строкой.
 */
public class ToStringConverter extends NullableStringConverter<String> {

    /**
     * Возвращает принятую строку.
     * @param s - строка
     * @return строка
     */
    @Override
    public String convertNotNullString(String s) {
        return s.trim();
    }
}
