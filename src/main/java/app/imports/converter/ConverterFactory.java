package app.imports.converter;

import app.table.DataType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Фабрика, создающая определённый {@link StringConverter} для указанного типа {@link DataType}.
 */
public class ConverterFactory {

    private static final ConverterFactory factory = new ConverterFactory();

    private final Map<DataType, StringConverter<?>> stringConverters;

    /**
     * Инициализация всех возможных {@link StringConverter}
     */
    {
        stringConverters = new EnumMap<>(DataType.class);
        stringConverters.put(DataType.STRING, new ToStringConverter());
        stringConverters.put(DataType.INTEGER, new ToIntegerConverter());
        stringConverters.put(DataType.LONG, new ToLongConverter());
        stringConverters.put(DataType.FLOAT, new ToFloatConverter());
        stringConverters.put(DataType.DOUBLE, new ToDoubleConverter());
        stringConverters.put(DataType.BOOLEAN, new ToBooleanConverter());
    }

    public static ConverterFactory getFactory(){
        return factory;
    }

    /**
     * Возвращает подходящий {@link StringConverter},
     *          который может преобразовать строки в тип, указанный в {@link DataType}.
     * @param toType - в какой тип нужно преобразовывать строки
     * @return подходящий конвертер
     */
    public StringConverter<?> getRightConverter(DataType toType){
        return stringConverters.get(toType);
    }
}
