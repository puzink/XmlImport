package app.xml;

import app.xml.exception.XmlElementParseException;

import java.util.List;

/**
 * Интерфейс для парсинга имени и атрибутов xml-элементов({@link Element}) из строки.
 * Пример элемента:
 * <table columns = "id; user_name" name = "users">
 * Имя элемента: "table"
 * Атрибуты: {"column":"id; user_name", "name":"users"}
 *
 * Строка с содержимым элемента считается корректной, если :
 *  1) вначале без отступов может быть специальный символ(например, '/') для определения типа элемента
 *  2) если вначале идет специальный символ, тогда между ним и именем элемента могут быть отступы.
 *      Если нет специального символа, тогда должно идти имя элемента без отступов вначале,
 *  3) после имени должен быть отступ,
 *  4) затем идут атрибуты.
 * Правила к атрибутам:
 *  1) значения должны быть заключены в двойные кавычки ("),
 *  2) ключ и значение атрибута должны быть разделены знаком равно ("="),
 *  3) атрибуты должны быть разделены как минимум 1 отступом,
 *  4) атрибуты должны иметь уникальные имена.
 * Правила задания имён элемента и ключей атрибутов:
 *  1) имена должны начинаться с буквы ({@link Character#isLetter(char)}),
 *  2) все остальные символы должны быть буквой либо символом ({@link Character#isLetterOrDigit(char)}).
 *
 */
public interface XmlElementParser {

    /**
     * Полностью разбирает строку, хранящую имя элемента и его атрибуты.
     * @param strElement - строка с содержимым элемента
     * @return элемент
     * @throws XmlElementParseException - в случае нарушения правил
     */
    Element parseElement(String strElement) throws XmlElementParseException;

    /**
     * Достаёт имя элемента из строки.
     * @param strElement - строка с содержимым элемента(можно без атрибутов)
     * @return имя элемента
     * @throws XmlElementParseException - если строка пустая либо нарушены правила
     *                                              (отступы вначале, некорректное имя).
     */
    String parseName(String strElement) throws XmlElementParseException;

    /**
     * Разбирает строку по атрибутам элемента.
     * Строка должна содержать только наборы ключ-значение.
     * Строка должна удовлетворять правилам задания атрибутов и их ключей.
     * @param str - строка с атрибутами
     * @return список атрибутов
     * @throws XmlElementParseException - если нарушено правило для атрибута, либо для его ключа.
     */
    List<Attribute> parseAttributes(String str) throws XmlElementParseException;

    /**
     * Определяет тип элемента ({@link ElementType}).
     * @param c - первый символ элемента
     * @return тип элемента
     */
    ElementType parseType(char c);

}
