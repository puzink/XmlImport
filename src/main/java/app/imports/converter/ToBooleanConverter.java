package app.imports.converter;

/**
 * Конвертирует строку в {@link Boolean}.
 */
public class ToBooleanConverter extends NullableStringConverter<Boolean> {

    /**
     * Конвертирует строку в {@link Boolean}.
     * Происходит сравнение с "true" и "false" без учёта регистра и отступов в начале и в конце строки.
     * @param s - строка
     * @return если строка равна "true" без учёта регистра - true,
     *          если равна "false" - false
     * @throws ClassCastException - если преобразовать не получается
     */
    @Override
    public Boolean convertNotNullString(String s) {
        if(s.trim().equalsIgnoreCase("true")){
            return true;
        }
        if(s.trim().equalsIgnoreCase("false")){
            return false;
        }
        throw new ClassCastException(
                String.format("Cannot convert string '%s' to boolean.", s)
        );
    }
}
