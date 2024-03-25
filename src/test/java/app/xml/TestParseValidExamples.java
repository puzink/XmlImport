package app.xml;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class TestParseValidExamples {

    private ClassLoader classLoader = TestParseNotValidExamples.class.getClassLoader();
    private String validXmlsDirectory = "xmls/valids/";
    private XmlElementParser elementParser = new XmlElementParserImpl();

    private List<Node> readAllNodes(File xml) throws IOException{
        try(XmlLazyParser parser = new XmlLazyParser(xml, elementParser)) {
            List<Node> res = new ArrayList<>();
            Node node;
            while (parser.hasNextNode()) {
                node = parser.getNextNode();
                res.add(node);
            }
            return res;
        }
    }

    private File getXmlFileForName(String fileName) throws URISyntaxException {;
        URI testDirectory = classLoader.getResource("").toURI();
        return new File(testDirectory.resolve(validXmlsDirectory).resolve(fileName));
    }

    @Test
    public void testValidTest() throws IOException, URISyntaxException{
        File xml = getXmlFileForName("double_close_tag.xml");

        Node table = new Node(null, new Element("table",List.of(), ElementType.OPEN));
        table.appendIntoBody(">" + System.lineSeparator());
        Node row = new Node(table, new Element("row", List.of(), ElementType.OPEN));
        Node id = new Node(row, new Element("id", List.of(), ElementType.OPEN));
        id.appendIntoBody("2");
        Node name = new Node(row, new Element("name", List.of(), ElementType.OPEN));
        name.appendIntoBody("Ole >g");
        List<Node> expected = List.of(table,row,id,name);

        List<Node> actual = readAllNodes(xml);

        Assertions.assertEquals(expected.size(), actual.size());
        for(int i = 0; i < expected.size(); ++i){
            Assertions.assertEquals(expected.get(i), actual.get(i));
        }

    }

    @Test
    public void testSpacesAfterLineInCloseElement() throws IOException, URISyntaxException{
        File xml = getXmlFileForName("spaces_after_line_in_close_element.xml");

        Node table = new Node(
                null,
                new Element("table",List.of(new Attribute("name", "users")), ElementType.OPEN)
        );
        Node row = new Node(table, new Element("row", List.of(), ElementType.OPEN));
        Node id = new Node(row, new Element("id", List.of(), ElementType.OPEN));
        id.appendIntoBody("2");
        Node name = new Node(row, new Element("name", List.of(), ElementType.OPEN));
        name.appendIntoBody("Oleg");
        List<Node> expected = List.of(table,row,id,name);

        List<Node> actual = readAllNodes(xml);

        Assertions.assertEquals(expected.size(), actual.size());
        for(int i = 0; i < expected.size(); ++i){
            Assertions.assertEquals(expected.get(i), actual.get(i));
        }

    }

    @Test
    public void testSeveralRows() throws URISyntaxException, IOException{
        File xml = getXmlFileForName("several_rows.xml");

        Node table = new Node(
                null,
                new Element("table",List.of(new Attribute("users", "321")), ElementType.OPEN)
        );
        Node row1 = new Node(table,
                new Element("row", List.of(new Attribute("val", "4561safd'")), ElementType.OPEN)
        );
        Node name1 = new Node(row1, new Element("name", List.of(), ElementType.OPEN));
        name1.appendIntoBody("Oleg");
        Node row2 = new Node(table,
                new Element("row", List.of(new Attribute("id", "65")), ElementType.OPEN)
        );
        Node name2 = new Node(row2, new Element("name", List.of(), ElementType.OPEN));
        name2.appendIntoBody("abcd");
        Node str = new Node(row2, new Element("str", List.of(), ElementType.OPEN));
        str.appendIntoBody("qwerty");
        List<Node> expected = List.of(table,row1,name1,row2,name2,str);

        List<Node> actual = readAllNodes(xml);

        Assertions.assertEquals(expected.size(), actual.size());
        for(int i = 0; i < expected.size(); ++i){
            Assertions.assertEquals(expected.get(i), actual.get(i));
        }
    }
}
