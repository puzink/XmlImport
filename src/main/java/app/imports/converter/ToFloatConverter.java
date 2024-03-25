package app.imports.converter;

/**
 * Конвертирует строку в {@link Float}.
 */
public class ToFloatConverter extends NullableStringConverter<Float> {

    /**
     * Конвертирует строку в {@link Float}.
     * @param s - строка
     * @return число
     * @throws NumberFormatException - если преобразовать не получается
     */
    @Override
    public Float convertNotNullString(String s) {
        try{
            return Float.valueOf(s.trim());
        }catch (Exception e){
            throw new IllegalArgumentException(
                    String.format("Cannot convert string '%s' to float.", s)
            );
        }
    }
}
