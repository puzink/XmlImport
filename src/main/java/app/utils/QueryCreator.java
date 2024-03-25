package app.utils;

import app.table.Column;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Вспомогательный класс, создающий запросы для {@link java.sql.PreparedStatement}.
 */
public class QueryCreator {

    /**
     * <p>Создает запрос, который проверяет наличие дублирующей строки в таблице по набору столбцов.
     * У строки "row" есть дубликат, если есть такая строка "tableRow" в таблице,
     *     что для каждого столбца "col" из указанных справедливо выражение:<br>
     *     <i>(row.col = tableRow.col) or
     *     (((row.col is null::integer)) + ((tableRow.col is null)::integer) = 2)</i>.
     *</p>
     * Генерирует запрос следующего вида:<br>
     *  <i>
     *      select case exists(<br>
     *                      select * from <b>tableName</b> as t<br>
     *                          where <b>сравнение строк по столбцам</b>)<br>
     *                      when True then True else False end<br>
     *                      from (values <b>строки</b>) as vals(<b>столбцы</b>))
     *
     *  </i>
     * @param rowSize количество строк, для которых будет проиходить поиск дублирующих
     * @param columns набор столбцов, по которому происходит сравнение
     * @param tableName название таблицы
     * @return запрос
     */
    public static String hasDuplicateStatement(int rowSize, List<Column> columns, String tableName){
        String tableAlies = "t";
        String rowValuesAlies = "vals";
        StringBuilder query = new StringBuilder()
                .append("select case exists(select * from ")
                .append(tableName).append(" as ").append(tableAlies)
                .append(" where ");
        List<String> valueComparisons = new ArrayList<>();
        for(Column uniqueCol : columns){

            String valueComparison = String.format("(((%s.%s is null)::integer + (%s.%s is null)::integer = 2) " +
                            " or " +
                            " (%s.%s = %s.%s)) ",
                    tableAlies, uniqueCol.getName(),
                    rowValuesAlies, uniqueCol.getName(),
                    tableAlies, uniqueCol.getName(),
                    rowValuesAlies, uniqueCol.getName()
            );

            valueComparisons.add(valueComparison);
        }
        query.append(String.join(" and ", valueComparisons));
        query.append(") when True then True else False end\n");

        query.append("from (values ");
        String rowValues = "(" + "?,".repeat(columns.size()-1) + "?)";
        query.append((rowValues + ",").repeat(rowSize - 1)).append(rowValues);
        query.append(") as ").append(rowValuesAlies).append("(");
        query.append(columns.stream()
                .map(Column::getName)
                .collect(Collectors.joining(","))
        );
        query.append(")");

        return query.toString();
    }

    /**
     * Создаёт запрос для вставки строк в таблицу.<br>
     * Вид запроса:<br>
     * <i>
     *     insert into [<b>имя таблицы</b>]([<b>набор столбцов</b>])<br>
     *          values <b>[значения строк]</b>
     * </i>
     * @param tableName имя таблицы
     * @param columns столбцы, по которым вставляются строки
     * @param rowsCount кол-во вставляемых строк
     * @return запрос на вставку строк
     */
    public static String insertRowStatement(String tableName, List<Column> columns, int rowsCount) {
        StringBuilder query = new StringBuilder().append("insert into ").append(tableName);
        String joinedColumns = columns.stream()
                .map(Column::getName)
                .collect(Collectors.joining(","));
        query.append("(").append(joinedColumns).append(") values ");
        String rowValue = "(" + "?,".repeat(columns.size()-1) + "?)";
        query.append((rowValue + ",").repeat(rowsCount - 1)).append(rowValue);
        return query.toString();
    }
}
