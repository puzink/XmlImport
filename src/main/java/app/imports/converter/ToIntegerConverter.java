package app.imports.converter;

/**
 * Конвертирует строку в {@link Integer}.
 */
public class ToIntegerConverter extends NullableStringConverter<Integer> {

    /**
     * Конвертирует строку в {@link Integer}.
     * @param s - строка
     * @return число
     * @throws NumberFormatException - если преобразовать не получается
     */
    @Override
    public Integer convertNotNullString(String s) {
        try{
            return Integer.valueOf(s.trim());
        }catch (Exception e){
            throw new IllegalArgumentException(
                    String.format("Cannot convert string '%s' to integer.", s)
            );
        }
    }
}
