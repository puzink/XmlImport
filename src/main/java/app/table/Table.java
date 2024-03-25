package app.table;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Таблица.
 */
@Getter
public class Table {

    private final String name;

    /**
     * Столбцы таблицы.
     */
    private final List<Column> columns;

    public Table(String name, List<Column> columns) {
        this.name = name;
        this.columns = columns;
    }

    public List<Column> getColumns(){
        return Collections.unmodifiableList(columns);
    }

}
