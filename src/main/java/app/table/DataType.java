package app.table;

import java.util.Arrays;
import java.util.Optional;

/**
 * Связь между типами в java и в postgresql.
 */
public enum DataType {
    INTEGER("integer"),
    DOUBLE("double precision"),
    FLOAT("real"),
    LONG("bigint"),
    STRING("character varying"),
    BOOLEAN("boolean");

    private final String sqlType;

    private DataType(String sqlType){
        this.sqlType = sqlType;
    }

    /**
     * Определяет тип по sql-типу в виде строкию
     * @param sqlType - нужный тип
     * @return Optional от нужного типа
     */
    public static Optional<DataType> getBySqlType(String sqlType){
        return Arrays.stream(DataType.values())
                .filter(dataType -> dataType.sqlType.equals(sqlType))
                .findFirst();
    }

}
