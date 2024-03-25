package app.xml;

import java.io.Closeable;
import java.io.IOException;

/**
 * Интерфейс для реализации парсера xml-файла.
 * Xml-файл можно представить в виде дерева, узлами({@link Node}) которого являются
 * xml-элементы({@link Element}) файла. У каждого узла есть открывающий и закрывающий элементы
 * и поддерево узлов-потомков.
 * Классы, реализующие этот интерфейс, должны проходить по узлам({@link Node}) дерева в глубину.
 * Пример:
 * <table>
 *     <row>
 *         <cell>cellValue</cell>
 *         <column>colValue</column>
 *     </row>
 *     <field>fValue</field>
 * </table>
 * В примере выше будут прочитаны узлы в следующем порядке: table->row->cell->column->field.
 **/
public interface XmlParser extends AutoCloseable {

    /**
     * Проверяет, есть ли непосещенные узлы в xml-файле.
     * Если значение равно false, значит все узлы в дереве пройдены и
     * файл полностью прочитан без ошибок.
     * @return true - если есть еще узлы в дереве. Иначе - false.
     * @throws IOException - если произошла ошибка во время парсинга.
     */
    boolean hasNextNode() throws IOException;

    /**
     * Возвращает следующий узел дерева при обходе в глубину со статусом {@link Node.NodeStatus#OPENED}.
     * Если при поиске нового узла встречаются закрывающие элементы открытых узлов, необходимо
     * менять их статус на {@link Node.NodeStatus#CLOSED}.
     * @return {@link app.xml.Node} - узел дерева
     * @throws IOException - если произошла ошибка во время чтения файла
     * @throws app.xml.exception.XmlNoMoreNodesException - если в дереве узлов больше нет непосещенных узлов
     * @throws app.xml.exception.XmlParseException - если произошла ошибка во время парсинга файла
     */
    Node getNextNode() throws IOException;

}
