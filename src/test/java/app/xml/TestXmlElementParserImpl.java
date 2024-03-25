package app.xml;

import app.xml.exception.XmlElementParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestXmlElementParserImpl {

    private final XmlElementParser elementParser = new XmlElementParserImpl();

    @Test
    public void testInvalidAttributeNames(){
        String attributeWithValue = "3name = \"rewd\"";
        Assertions.assertThrows(XmlElementParseException.class,
                ()-> elementParser.parseAttributes(attributeWithValue),
                "Name must start with letter."
        );

        String unexpectedSymbolInName = "na]me = \"rewd\"";
        Assertions.assertThrows(XmlElementParseException.class,
                ()-> elementParser.parseAttributes(unexpectedSymbolInName),
                "Name must contain digits and letters only."
        );

        String emptyName = "= \"rewd\"";
        Assertions.assertThrows(XmlElementParseException.class,
                ()-> elementParser.parseAttributes(emptyName),
                "Name must not be empty."
        );

        String notClosedValue = "name = \"val";
        Assertions.assertThrows(XmlElementParseException.class,
                ()-> elementParser.parseAttributes(notClosedValue),
                "Attribute value is not closed."
        );
    }

    @Test
    public void testValidAttributeNames() throws XmlElementParseException {
        String nameWithDigits = "na32me = \"rewd\"";
        Assertions.assertEquals("na32me", elementParser.parseName(nameWithDigits));

        String oneLetterName = "n = \"rewd\"";
        Assertions.assertEquals("n", elementParser.parseName(oneLetterName));
    }

    @Test
    public void testAttributeParsing() throws XmlElementParseException {
        String str = "name1 = \"val1\" name2 = \"2\"";
        List<Attribute> expected =
                List.of(new Attribute("name1", "val1"), new Attribute("name2", "2"));
        List<Attribute> actual = elementParser.parseAttributes(str);
        Assertions.assertEquals(expected.size(), actual.size());
        Assertions.assertArrayEquals(expected.toArray(), elementParser.parseAttributes(str).toArray());
    }

    @Test
    public void testEmptyAttributes() throws XmlElementParseException {
        String str = "    \t\n";
        Assertions.assertTrue(elementParser.parseAttributes(str).isEmpty());
    }

    @Test
    public void testAttributeUniqueness(){
        String str = "attr = \"1\" attr2 = \"abs\" \n attr = \"hd\"";
        Assertions.assertThrows(XmlElementParseException.class,
                ()->elementParser.parseAttributes(str),
                "Duplicate attribute 'attr'."
        );
    }


}
