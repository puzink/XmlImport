package app.xml;

import app.xml.exception.XmlNoMoreNodesException;
import app.xml.exception.XmlParseException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class ListParser implements XmlParser {

    private final List<Element> elements;
    private Iterator<Element> elementIterator;

    private List<String> nodeBodies;
    private Iterator<String> nodeBodiesIterator;

    private int nodeCount = 0;

    private NodePath nodePath;

    public ListParser(List<Element> elements, List<String> nodeBodies){
        this.elements = elements;
        nodeCount = countNodes(elements);
        this.elementIterator = elements.iterator();
        this.nodeBodies = nodeBodies;
        this.nodeBodiesIterator = nodeBodies.iterator();
        nodePath = new NodePath();
    }

    private int countNodes(List<Element> elements){
        int result = 0;
        for(Element element : elements){
            if(element.getType() == ElementType.OPEN){
                result++;
            }
        }
        return result;
    }

    @Override
    public boolean hasNextNode() throws IOException {
        return nodeCount > 0;
    }

    @Override
    public Node getNextNode() throws IOException {
        if(!hasNextNode()){
            throw new XmlNoMoreNodesException("There is no more nodes.", null);
        }
        Element nextElement = elementIterator.next();
        if(nextElement.getType() == ElementType.CLOSE){
            if(checkNodeClose(nextElement)){
                return getNextNode();
            }
        }
        Node node = new Node(nodePath.getTailNode(), nextElement, Node.NodeStatus.OPENED);
        node.appendIntoBody(nodeBodiesIterator.next());
        nodePath = nodePath.addNode(node);
        nodeCount--;
        return node;
    }

    private boolean checkNodeClose(Element element) throws XmlParseException {
        if(nodePath.isEmpty()){
            throw new XmlParseException("Close element before open.");
        }
        if(!nodePath.getTailNode().getName().equals(element.getName().trim())){
            throw new XmlParseException("Close element name does not equal to the current node one.");
        }
        nodePath.getTailNode().setStatus(Node.NodeStatus.CLOSED);
        nodePath = nodePath.removeLast();
        return true;
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("Cannot close list.");
    }
}
