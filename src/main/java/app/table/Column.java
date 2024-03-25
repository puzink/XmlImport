package app.table;

import lombok.Getter;

import java.util.Objects;

/**
 * Столбец таблицы.
 */
@Getter
public class Column {

    private String name;
    private DataType type;

    public Column(String name) {
        this.name = name;
    }

    public Column(String name, DataType type){
        this.name = name;
        this.type = type;
    }

    @Override
    public int hashCode(){
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o == null){
            return false;
        }
        if(!(o instanceof Column)){
            return false;
        }
        Column other = (Column) o;

        return Objects.equals(other.name, name);
    }

}
