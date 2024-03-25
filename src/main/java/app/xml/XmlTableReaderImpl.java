package app.xml;

import app.table.Row;
import app.xml.exception.XmlUnexpectedElementMetException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Класс для считывания строк(наборов пар ключ-значение {@link Row}) таблицы из xml-файла,
 * в котором корневой узел({#link Node}) имеет имя "table".
 * Затем идут строки - узлы с именем "row". У каждой строки может быть любое
 * кол-во дочерних узлов, имена которых считаются ключом, а тело узла - значением.
 * Все вложенные узлы в узел "row" являются значениями строки, независимо от глубины вложенности.
 * Пример:
 * <table>
 *     <row>                    <- строка с парами {"id":"1", "name":"Abc",
 *         <id>1</id>                                   "city":"Moscow", "country":"Russia"}
 *         <name>Abc</name>
 *         <address>
 *             <city>Moscow</city>
 *             <country>Russia</country>
 *         </address>
 *     </row>
 * </table>
 */
public class XmlTableReaderImpl implements XmlTableReader {

    /**
     * Xml-парсер
     */
    private final XmlParser xmlParser;

    /**
     * Сслыка на текущий узел "row", значения которого нужно считать.
     * Она необходима в следующей ситуации:
     *  если во время чтения дочерних узлов строки был прочитан узел "row" следующей,
     *  тогда её необходимо запомнить, чтобы при следующем вызове {@link #readRow()}
     *  сразу начинать считывать дочерние узлы.
     */
    private Node currentRowNode = null;

    /**
     * Ссылка на предыдущую считанную строку. Связана с текущей строкой ({@link #currentRowNode}
     * тем что, если они совпадают(==),
     * тогда начало новой строки не было считано("<row>") и его необходимо найти.
     */
    private Node prevRowNode = null;

    /**
     * Ссылка на табличный узел. Нужен для получения атрибутов и тела узла.
     * @see Node
     * @see #getTable()
     */
    private Node tableNode = null;

    /**
     * Названия узлов для таблицы и строк.
     */
    private static final String TABLE_ELEMENT_NAME = "table";
    private static final String ROW_ELEMENT_NAME = "row";


    public XmlTableReaderImpl(XmlParser xmlParser){
        this.xmlParser = xmlParser;
    }

    /**
     * Возвращает узел таблицы. Если он не был прочитан - считывает из парсера.
     * Табличный узел должен быть корневым.
     * @return табличный узел
     * @throws IOException - если произошла ошибка в парсере
     */
    @Override
    public Node getTable() throws IOException {
        if(tableNode == null){
            tableNode = readTableElement();
        }
        return tableNode;
    }

    /**
     * Возвращает следующую строку таблицы. Если больше нет строк в таблице - null.
     * Все дочерние узлы строки считаются её значениями.
     * Если встречаются несколько узлов с одинаковым именем - сохраняется значение последнего из них.
     * @return следующую строку. null - если строк больше нет
     * @throws XmlUnexpectedElementMetException - если имя узла строки не {@link #ROW_ELEMENT_NAME}
     * @throws IOException - если произошла ошибка в парсере
     */
    @Override
    public Row readRow() throws IOException{
        if(tableNode == null){
            tableNode = readTableElement();
        }
        /*
         * Если текущая еще не найдена(=null), либо ссылается на предыдущую считанную,
         * тогда необходимо считать её.
         */
        if(currentRowNode == null || prevRowNode == currentRowNode){
            currentRowNode = xmlParser.hasNextNode()
                    ? xmlParser.getNextNode()
                    : null;
        }
        /*
         *  Если ссылка == null, тогда строк больше нет в файле.
         */
        if(currentRowNode == null){
            return null;
        }
        /*
         *   Если имя строки != "row", тогда ошибка
         */
        if(!currentRowNode.getElement().getName().equals(ROW_ELEMENT_NAME)){
            throw new XmlUnexpectedElementMetException(
                    String.format("A non-row element('%s') was encountered in the table",
                            currentRowNode.getElement().getName()
                    )
            );
        }

        /*
         * Если узел новой строки сразу завершился, значит строка пустая: <row></row>
         */
        if(currentRowNode.isClosed()){
            prevRowNode = currentRowNode;
            return new Row(new ArrayList<>());
        }

        // дочерние узлы
        List<Node> nestedNodesInRow = new ArrayList<>();
        Node cell = null;
        while(currentRowNode.isOpened()){
            /*
             * Если узлов больше нет, и ошибки не произошло, значит строка закрылась,
             * и у неё нет больше дочерних узлов.
             */
            if(!xmlParser.hasNextNode()){
                currentRowNode = null;
                break;
            }

            // считывание дочернего узла
            cell = xmlParser.getNextNode();
            if(isNewRow(cell)){
                prevRowNode = currentRowNode;
                currentRowNode = cell;
                return new Row(nestedNodesInRow);
            }
            nestedNodesInRow.add(cell);
        }

        prevRowNode = currentRowNode;

        return new Row(nestedNodesInRow);
    }

    /**
     * Проверяет, является ли узел новой строкой таблицы.
     * @param node - узел на проверку
     * @return true - является новой строкой, false - иначе.
     */
    private boolean isNewRow(Node node) {
        return Objects.equals(node.getParent(), currentRowNode.getParent());
    }

    /**
     * Считывает табличный узел и проверяет его имя.
     * @return узел, соответствующий табличному
     * @throws XmlUnexpectedElementMetException - если имя узла не {@link #TABLE_ELEMENT_NAME}
     * @throws IOException - если произошла ошибка в парсере
     */
    private Node readTableElement() throws IOException{
        Node node = xmlParser.getNextNode();
        if(!node.getElement().getName().equals(TABLE_ELEMENT_NAME)){
            throw new XmlUnexpectedElementMetException(
                    String.format("XML does not start with table element. Met: %s",
                            node.getElement().getName()
                    )
            );
        }
        return node;
    }


    @Override
    public void close() throws Exception {
        xmlParser.close();
    }
}
