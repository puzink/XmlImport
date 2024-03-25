package app.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * Упорядоченная последовательность узлов в дереве.
 * При изменении последовательности(удаление, добавление узла)
 *      создается новый экземпляр {@code NodePath}.
 */
public class NodePath{

    /**
     * Последовательность узлов.
     */
    private final List<Node> nodes;

    public NodePath(List<Node> nodes) {
        this.nodes = nodes;
    }

    public NodePath(Node... nodes){
        this.nodes = List.of(nodes);
    }

    public NodePath(NodePath nodePath){
        this.nodes = List.copyOf(nodePath.nodes);
    }

    /**
     * Статический фабричный метод.
     */
    public static NodePath pathOf(List<Node> nodes){
        return new NodePath(nodes);
    }

    /**
     * Создает новый экземпляр {@link NodePath},
     * который содержит все узлы текущего объекта в том же порядке и новый узел в конце.
     * @param node - узел на добавление в конец
     * @return новый эземпляр, который содержит все узлы текущего объекта в том же порядке и
     *              новый узел в конце
     */
    public NodePath addNode(Node node){
        List<Node> newPath = new ArrayList<>(nodes);
        newPath.add(node);
        return new NodePath(newPath);
    }

    /**
     * Вставляет символ в тело последнего узла в последовательности.
     * @param c - символ, который нужно вставить
     */
    public void appendIntoBody(char c) {
        if(isEmpty()){
            throw new ArrayIndexOutOfBoundsException("Node path is empty.");
        }
        getTailNode().appendIntoBody(c);
    }

    /**
     * Возвращает последний узел в последовательности.
     * @return последний узел
     */
    public Node getTailNode(){
        if(isEmpty()){
            return null;
        }
        return nodes.get(nodes.size() - 1);
    }

    public boolean isEmpty(){
        return nodes.isEmpty();
    }

    /**
     * Возвращает новый экземпляр {@link NodePath} без последнего узла.
     */
    public NodePath removeLast() {
        return new NodePath(nodes.subList(0, nodes.size()-1));
    }
}
