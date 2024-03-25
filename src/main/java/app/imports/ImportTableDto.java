package app.imports;

import app.table.Column;
import app.table.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для транспортировки таблицы, набора столбцов, по которым будут вставляться строки в БД,
 *  и набора столбцов, по которым будут проверяться уникальность строк.
 */
@Data
@NoArgsConstructor
public class ImportTableDto {

    private Table table;

    /**
     * Набор уникальных столбцов.
     */
    private List<Column> uniqueColumns;

    /**
     * Набор столбцов, по которым будет происходить вставка строк.
     */
    private List<Column> columnsForInsert;

    public ImportTableDto(Table table, List<Column> uniqueColumns, List<Column> columnsForInsert) {
        this.table = table;
        this.uniqueColumns = uniqueColumns;
        this.columnsForInsert = columnsForInsert;
    }

}
