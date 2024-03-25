package app.imports.converter;

/**
 * Конвертирует строку в {@link Double}.
 */
public class ToDoubleConverter extends NullableStringConverter<Double> {

    /**
     * Конвертирует строку в {@link Double}.
     * @param s - строка
     * @return число
     * @throws NumberFormatException - если преобразовать не получается
     */
    @Override
    public Double convertNotNullString(String s) {
        try{
            return Double.valueOf(s.trim());
        }catch (Exception e){
            throw new IllegalArgumentException(
                    String.format("Cannot convert string '%s' to double.", s)
            );
        }
    }
}
