package app.xml;

import app.xml.exception.XmlElementParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Реализация парсера из строки в {@link Element}.
 */
public class XmlElementParserImpl implements XmlElementParser {

    /**
     * Полностью разбирает строку, хранящую имя элемента и его атрибуты.
     * @param strElement - строка с содержимым элемента
     * @return элемент
     * @throws XmlElementParseException - в случае нарушения правил
     */
    @Override
    public Element parseElement(String strElement) throws XmlElementParseException {
        checkElementIsEmpty(strElement);
        String name = parseName(strElement);
        int attributeStart = strElement.indexOf(name) + name.length();
        List<Attribute> attributes = parseAttributes(strElement.substring(attributeStart));
        ElementType type = parseType(strElement.charAt(0));
        return new Element(name, attributes, type);
    }

    /**
     * Достаёт имя элемента из строки.
     * @param strElement - строка с содержимым элемента(можно без атрибутов)
     * @return имя элемента
     * @throws XmlElementParseException - если строка пустая либо нарушены правила
     *                                              (отступы вначале, некорректное имя).
     */
    @Override
    public String parseName(String strElement) throws XmlElementParseException {
        checkElementIsEmpty(strElement);

        int nameStart = findFirstNotWhitespaceChar(strElement, 0);
        if(nameStart > 0){
            throw new XmlElementParseException("Whitespaces before element name.");
        }
        // между '/' и именем элемента могут быть отступы: </   table>
        if (hasElementTypeSymbol(strElement, nameStart)) {
            nameStart = findFirstNotWhitespaceChar(strElement, nameStart + 1);
        }
        if(nameStart < 0){
            throw new XmlElementParseException("Element name is not found.");
        }
        int nameEnd = findWhitespace(strElement, nameStart + 1);
        nameEnd = (nameEnd < 0) ? strElement.length() : nameEnd;

        String name = strElement.substring(nameStart, nameEnd).trim();
        validateName(name);

        return name;
    }


    /**
     * Разбирает строку по атрибутам элемента.
     * Строка должна содержать только наборы ключ-значение.
     * Строка должна удовлетворять правилам задания атрибутов и их ключей.
     * @param str - строка с атрибутами
     * @return список атрибутов
     * @throws XmlElementParseException - если нарушено 1 из правил для атрибута, либо для его ключа.
     */
    @Override
    public List<Attribute> parseAttributes(String str) throws XmlElementParseException {
        List<Attribute> result = new ArrayList<>();

        int attributeKeyStart = findFirstNotWhitespaceChar(str,0);
        while(attributeKeyStart != -1){
            int equalSign = str.indexOf('=', attributeKeyStart);
            if(equalSign == -1){
                throw new XmlElementParseException("Attribute must has a value.");
            }

            int attrValueStart = str.indexOf("\"", equalSign + 1);
            if(attrValueStart == -1 && !str.substring(equalSign + 1).isBlank()){
                throw new XmlElementParseException("Attribute value must be enclosed in double quotes.");
            }
            if(attrValueStart == -1){
                throw new XmlElementParseException("Attribute must has a value.");
            }

            int attrValueEnd = str.indexOf("\"", attrValueStart + 1);
            if(attrValueEnd == -1){
                throw new XmlElementParseException("Attribute value is not closed.");
            }
            if(attrValueEnd + 1 < str.length() && !Character.isWhitespace(str.charAt(attrValueEnd + 1))){
                throw new XmlElementParseException("There must be a whitespace between attributes.");
            }

            String key = str.substring(attributeKeyStart, equalSign).trim();
            validateName(key);
            String value = str.substring(attrValueStart + 1, attrValueEnd);
            result.add(new Attribute(key, value));

            checkAttributeUniqueness(result, key);

            attributeKeyStart = findFirstNotWhitespaceChar(str, attrValueEnd + 1);
        }

        return result;
    }

    /**
     * Проверяет на уникальность атрибута.
     * Если атрибут не уникален - возникает исключительная ситуация.
     * @param attributes - список атрибутов
     * @param attributeName - имя уникального атрибута
     * @throws XmlElementParseException - если атрибут не уникален
     */
    private void checkAttributeUniqueness(List<Attribute> attributes, String attributeName)
            throws XmlElementParseException {
        long attrCount = attributes.stream()
                .filter(Attribute.filterByName(attributeName))
                .count();

        if(attrCount > 1){
            throw new XmlElementParseException(
                    String.format(
                            "Duplicate attribute '%s'.",
                            attributeName
                    )
            );
        }
    }

    /**
     * Определяет тип элемента ({@link ElementType}).
     * @param c - первый символ элемента
     * @return тип элемента
     */
    @Override
    public ElementType parseType(char c){
        if(c == '/'){
            return ElementType.CLOSE;
        }
        if(c == '?'){
            return ElementType.PROLOG;
        }
        return ElementType.OPEN;
    }

    /**
     * Проверяет есть в начале элемента специальный символ.
     * @param str - элемент
     * @param nameStart - начало элемента
     * @return true - есть, false - нет
     */
    private boolean hasElementTypeSymbol(String str, int nameStart) {
        char c = str.charAt(nameStart);
        return c == '/' || c == '?';
    }

    /**
     * Проверяет имя на корректность.
     * Правила задания имён элемента и ключей атрибутов:
     *  1) имена должны начинаться с буквы ({@link Character#isLetter(char)}),
     *  2) все остальные символы должны быть буквой либо символом
     *                          ({@link Character#isLetterOrDigit(char)}).
     * @param name - имя элемента или ключ атрибута.
     * @throws XmlElementParseException - если имя не соответствует правилам, либо пусто.
     */
    private void validateName(String name) throws XmlElementParseException {
        if(name.isBlank()){
            throw new XmlElementParseException("Name must not be empty.");
        }
        char firstLetter = name.charAt(0);
        if(!Character.isLetter(firstLetter)){
            throw new XmlElementParseException("Name must start with letter.");
        }
        for(int i = 1; i < name.length();++i){
            char c = name.charAt(i);
            if(!Character.isLetterOrDigit(c)){
                throw new XmlElementParseException("Name must contain digits and letters only.");
            }
        }
    }

    /**
     * Находит первый символ в строке
     *      не являющийся отступом({@link Character#isWhitespace(char) == false}),
     *      начиная с заданной позиции.
     * @param str - строка для поиска
     * @param from - позиция, с которой начинается поиск
     * @return позиция символа в строке. -1 - если символа нет, либо from >= str.length().
     */
    private int findFirstNotWhitespaceChar(String str, int from){
        if(from >= str.length()){
            return -1;
        }
        for(int i = from;i < str.length();++i){
            if(!Character.isWhitespace(str.charAt(i))){
                return i;
            }
        }
        return -1;
    }

    /**
     * Поиск отступа({@link Character#isWhitespace(char) == true}) в строке,
     *      начиная с заданной позиции.
     * @param str - строка для поиска
     * @param from - позиция, с которой начинается поиск
     * @return позиция символа в строке. -1 - если отступа нет, либо from >= str.length().
     */
    private int findWhitespace(String str, int from) {
        if(from >= str.length()){
            return -1;
        }
        for(int i = from; i < str.length();++i){
            if(Character.isWhitespace(str.charAt(i))){
                return i;
            }
        }
        return -1;
    }

    /**
     * Проверяет, пуст ли элемент.
     * @param str - элемент
     * @throws XmlElementParseException - элемент пуст
     */
    private void checkElementIsEmpty(String str) throws XmlElementParseException {
        if(str.isBlank()){
            throw new XmlElementParseException("Element must not be empty.");
        }
    }
}
