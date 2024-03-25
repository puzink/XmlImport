package app.imports.converter;

/**
 * Конвертирует строку в {@link Long}.
 */
public class ToLongConverter extends NullableStringConverter<Long> {

    /**
     * Конвертирует строку в {@link Long}.
     * @param s - строка
     * @return число
     * @throws NumberFormatException - если преобразовать не получается
     */
    @Override
    public Long convertNotNullString(String s) {
        try{
            return Long.valueOf(s.trim());
        }catch (Exception e){
            throw new IllegalArgumentException(
                    String.format("Cannot convert string '%s' to long.", s)
            );
        }
    }
}
