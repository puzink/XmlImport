package app.xml;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Объекты класса являются узлами дерева, построенного при парсинге xml-файла.
 * У каждого узла есть: 1)открывающий элемент{@link Element},
 *      2) тело - символы между открывающим и закрывающим элементами,
 *      3) статус{@link NodeStatus} -
 *          если {@code NodeStatus.OPENED} - тело может содержать не все символы узла,
 *          если {@code NodeStatus.CLOSED} - тело содержит все символы узла
 *      4) родитель в дереве.
 *
 * Пример:
 *      <table>                 <- родитель узла row
 *          <row id="1">        <- открывающий элемент row
 *              value           <- тело узла row
 *          </row>
 *      </table>
 */
@Getter
public class Node {
    /**
     * Открывающий элемент узла.
     */
    private final Element element;
    /**
     * Тело узла: содержит символы, находящиеся между элементами узла.
     */
    private final StringBuilder body;

    @Setter
    private NodeStatus status = NodeStatus.CLOSED;
    /**
     * Ссылка на родителя узла. Может быть null, если является корневым элементом.
     */
    private final Node parent;

    public Node(Node parent, Element element, NodeStatus status){
        this(parent, element);
        this.status = status;
    }
    
    public Node(Node parent, Element element){
        this.parent = parent;
        this.element = element;
        body = new StringBuilder();
    }

    /**
     * Возвращает имя узла, находящееся в его открывающем элементе.
     * @return имя узла
     */
    public String getName(){
        return element.getName();
    }

    /**
     * Вставляет символы в конец тела.
     */
    public void appendIntoBody(char c){
        body.append(c);
    }
    public void appendIntoBody(String s){
        body.append(s);
    }

    public boolean isBodyEmpty() {
        return body.length() == 0;
    }

    public String getStringBody(){
        return body.toString();
    }

    public String toString(){
        return String.format("Name:%s, elem = '%s', body='%s'", element.getName(), element, body.toString());
    }

    public boolean equals(Object o){
        if(o == null){
            return false;
        }
        if(o.getClass() != this.getClass()){
            return false;
        }
        Node other = (Node) o;
        return Objects.equals(other.getParent(), parent)
                && Objects.equals(body.toString(), other.getBody().toString())
                && Objects.equals(element, other.getElement());

    }

    public boolean isOpened() {
        return status == NodeStatus.OPENED;
    }

    public boolean isClosed() {
        return status == NodeStatus.CLOSED;
    }


    /**
     * Статус узла.
     * Если статус = OPENED, тогда тело узла может содержать не все символы внутри узла.
     * Эта ситуация возможна, если узлы не полностью читаются из файла.
     * Если статус = CLOSED, тогда тело содержит все символы внутри узла.
     * @see XmlLazyParser
     */
    public enum NodeStatus{
        OPENED, CLOSED;
    }
}
