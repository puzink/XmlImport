package app.xml;

import app.xml.exception.XmlParseException;
import app.xml.exception.XmlElementParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class TestParseNotValidExamples {

    private ClassLoader classLoader = TestParseNotValidExamples.class.getClassLoader();
    private String notValidXmlsDirectory = "xmls/withErrors/";
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
        return new File(testDirectory.resolve(notValidXmlsDirectory).resolve(fileName));
    }

    @Test
    public void testDoubleOpenTag() throws URISyntaxException {
        String xmlName = "double_open_tag.xml";
        File xml = getXmlFileForName(xmlName);

        Assertions.assertThrows(
                XmlParseException.class,
                () -> readAllNodes(xml),
                "Double open tag."
        );
    }

    @Test
    public void testCloseElementBeforeOpen() throws URISyntaxException {
        String xmlName = "close_element_before_open.xml";
        File xml = getXmlFileForName(xmlName);

        Assertions.assertThrows(
                XmlParseException.class,
                () -> readAllNodes(xml),
                "Close element name does not equal to the current node one."
        );
    }

    @Test
    public void testEndOfFileInElement() throws URISyntaxException {
        String xmlName = "end_of_file_inside_element.xml";
        File xml = getXmlFileForName(xmlName);

        Assertions.assertThrows(
                XmlParseException.class,
                () -> readAllNodes(xml),
                "Unexpected file end."
        );
    }

    @Test
    public void testLeadingSpacesInCloseElement() throws URISyntaxException {
        String xmlName = "leading_spaces_in_close_element.xml";
        File xml = getXmlFileForName(xmlName);

        Assertions.assertThrows(
                XmlElementParseException.class,
                () -> readAllNodes(xml),
                "Whitespaces before element name."
        );
    }

    @Test
    public void testLeadingSpacesInElement() throws URISyntaxException {
        String xmlName = "leading_spaces_in_element.xml";
        File xml = getXmlFileForName(xmlName);

        Assertions.assertThrows(
                XmlElementParseException.class,
                () -> readAllNodes(xml),
                "Whitespaces before element name."
        );
    }

    @Test
    public void testUnexpectedSymbolBeforeRoot() throws URISyntaxException {
        String xmlName = "unexpected_symbol_before_root.xml";
        File xml = getXmlFileForName(xmlName);

        Assertions.assertThrows(
                XmlParseException.class,
                () -> readAllNodes(xml),
                "Unexpected symbol occurs."
        );
    }

    @Test
    public void testSymbolAfterEnd() throws URISyntaxException {
        String xmlName = "symbols_after_end.xml";
        File xml = getXmlFileForName(xmlName);

        Assertions.assertThrows(
                XmlParseException.class,
                () -> readAllNodes(xml),
                "Unexpected symbol occurs."
        );
    }

    @Test
    public void testElementAfterXmlEnd() throws URISyntaxException {
        String xmlName = "element_after_xml_end.xml";
        File xml = getXmlFileForName(xmlName);

        Assertions.assertThrows(
                XmlParseException.class,
                () -> readAllNodes(xml),
               "Multiply root elements."
        );
    }

    @Test
    public void testCloseElementWithoutName() throws URISyntaxException {
        String xmlName = "close_element_without_name.xml";
        File xml = getXmlFileForName(xmlName);

        Assertions.assertThrows(
                XmlElementParseException.class,
                () -> readAllNodes(xml),
                "Wrong element name."
        );
    }

    @Test
    public void testAttributeWithoutEqualSign() throws URISyntaxException {
        String xmlName = "attribute_without_equal_sign.xml";
        File xml = getXmlFileForName(xmlName);

        Assertions.assertThrows(
                XmlElementParseException.class,
                () -> readAllNodes(xml),
                "Attribute must has a value."
        );
    }

    @Test
    public void testAttributeWithoutValue() throws URISyntaxException {
        String xmlName = "attribute_without_value.xml";
        File xml = getXmlFileForName(xmlName);

        Assertions.assertThrows(
                XmlElementParseException.class,
                () -> readAllNodes(xml),
                "Attribute must has a value."
        );
    }

    @Test
    public void testAttributeWithoutKey() throws URISyntaxException {
        String xmlName = "attribute_without_key.xml";
        File xml = getXmlFileForName(xmlName);

        Assertions.assertThrows(
                XmlElementParseException.class,
                () -> readAllNodes(xml),
                "Attribute name must not be empty."
        );
    }

    @Test
    public void testAttributeValueWithoutQuotes() throws URISyntaxException {
        String xmlName = "attribute_value_without_quotes.xml";
        File xml = getXmlFileForName(xmlName);

        Assertions.assertThrows(
                XmlElementParseException.class,
                () -> readAllNodes(xml),
                "Attribute value must be enclosed in double quotes."
        );
    }

    @Test
    public void testElementNameWithEqualSign() throws URISyntaxException {
        String xmlName = "element_name_with_equal_sign.xml";
        File xml = getXmlFileForName(xmlName);

        Assertions.assertThrows(
                XmlElementParseException.class,
                () -> readAllNodes(xml),
                "Name must contain digits and letters only."
        );
    }

    @Test
    public void testAttributeValueWithSingleQuotes() throws URISyntaxException {
        String xmlName = "value_with_single_quotes.xml";
        File xml = getXmlFileForName(xmlName);

        Assertions.assertThrows(
                XmlElementParseException.class,
                () -> readAllNodes(xml),
                "Attribute value must be enclosed in double quotes."
        );
    }
}
