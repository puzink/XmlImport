package app.table;

import app.xml.Node;

import java.util.*;
import java.util.function.Supplier;

/**
 * Строка таблицы.
 */
public class Row {

    /**
     * Значения строки. Ключ - имя столбца.
     */
    private Map<String, Object> values;

    public Row(Map<String, Object> values){
        this.values = values;
    }

    /**
     * Создание строки по xml-узлам.
     * В качестве столбца выступает имя узла, а в качестве значения - тело узла.
     * @param nodes - xml-узлы.
     */
    public Row(List<Node> nodes){
        Supplier<Map<String,Object>> supp = HashMap::new;
        this.values = nodes.stream()
                .collect(
                        supp,
                        (map, node) -> map.put(node.getName(), node.getStringBody()),
                        Map::putAll
                );
    }

    public Map<String, Object> getValues(){
        return values;
    }

    /**
     * Добавляет новую пару столбец со значением в строку.
     * Если уже есть пара с таким столбцом, тогда заменяется значение на новое.
     * @param column - столбец
     * @param newValue - значение
     * @return если столбец уже был, тогда старое значение. Иначе - новое.
     */
    public Object addValue(String column, Object newValue){
        Object oldValue = values.get(column);
        values.put(column, newValue);
        return oldValue != null ? oldValue : newValue;
    }

    @Override
    public boolean equals(Object o){
        if(o == null){
            return false;
        }
        if(!(o instanceof Row)){
            return false;
        }
        Row other = (Row) o;
        return Objects.equals(values, other.values);
    }

    @Override
    public String toString(){
        return "Row:" + values.toString();
    }

    @Override
    public int hashCode(){
        return values.hashCode();
    }

    /**
     * Проверяет наличие столбца в строке.
     * @param column - искомый столбец
     * @return есть - true. Иначе - false
     */
    public boolean containsColumn(Column column){
        return containsColumn(column.getName());
    }

    /**
     * Проверяет наличие столбца в строке.
     * @param columnName - имя столбеца
     * @return есть - true. Иначе - false
     */
    public boolean containsColumn(String columnName) {
        return values.containsKey(columnName);
    }

    /**
     * Проверяет наличие набора столбцов в строке.
     * @param columns - столбцы
     * @return если содержатся все из них - true. Иначе - false
     */
    public boolean containsColumns(Collection<Column> columns) {
        return columns.stream()
                .map(Column::getName)
                .allMatch(this::containsColumn);
    }

    /**
     * Возвращает значение по столбцу.
     * Если столбца нет - null.
     * @param col -столбце
     * @return значение. Если столбца нет - null.
     */
    public Object get(Column col){
        return values.get(col.getName());
    }

    /**
     * Проецирует строку на указанные столбцы. При этом создается новый экземпляр {@link Row},
     *      который содержит эти столбцы со значениями из строки.
     * Те требуемые столбцы, которые отсутствуют в строке, пропускаются.
     * @param columns - требуемые для проекции столбцы
     * @return новая строка, которая является проекцией текущей на указанный набор строк.
     */
    public Row projectOnto(Collection<Column> columns){
        Map<String, Object> projectionValues = new HashMap<>();
        for(Column col : columns){
            if(containsColumn(col)){
                projectionValues.put(col.getName(), get(col));
            }
        }
        return new Row(projectionValues);
    }
}
