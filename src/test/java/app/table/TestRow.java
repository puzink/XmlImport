package app.table;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

public class TestRow {

    @Test
    public void testColumnContains(){
        Row row = new Row(Map.of("first", 10, "second", "asg"));
        Assertions.assertFalse(row.containsColumn(new Column("First")));
        Assertions.assertFalse(row.containsColumn(new Column("aaa")));

        List<Column> existedColumns = List.of(new Column("second"), new Column("first"));
        existedColumns.forEach(col -> Assertions.assertTrue(row.containsColumn(col)));
        Assertions.assertTrue(row.containsColumns(existedColumns));

        List<Column> moreColumns = createList(existedColumns, new Column("sss"));
        Assertions.assertFalse(row.containsColumns(moreColumns));

        List<Column> copyColumns = createList(existedColumns, existedColumns.get(0));
        Assertions.assertTrue(row.containsColumns(copyColumns));

        List<Column> emptyList = List.of();
        Assertions.assertTrue(row.containsColumns(emptyList));
    }

    @Test
    public void testProject(){
        Row row = new Row(Map.of("first", 10, "second", "asg"));

        Row firstColExpected = new Row(Map.of("first", 10));
        Assertions.assertEquals(firstColExpected, row.projectOnto(List.of(new Column("first"))));
        Row secondColExpected = new Row(Map.of("second", "asg"));
        Assertions.assertEquals(secondColExpected, row.projectOnto(List.of(new Column("second"))));

        Row emptyRow = new Row(Map.of());
        Assertions.assertEquals(emptyRow, row.projectOnto(List.of()));

        List<Column> rowColumns = row.getValues().keySet().stream()
                .map(Column::new)
                .collect(Collectors.toList());
        Assertions.assertEquals(row, row.projectOnto(rowColumns));

        List<Column> moreColumns = createList(rowColumns, new Column("third"));
        Assertions.assertEquals(row, row.projectOnto(moreColumns));
    }

    private <T> List<T> createList(List<? extends T> list, T... extraElems){
        List<T> result = new ArrayList<>(list);
        result.addAll(List.of(extraElems));
        return result;
    }
}
