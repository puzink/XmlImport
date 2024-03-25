package app.xml.exception;

import app.xml.CursorPosition;

/**
 * Исключительная ситуация, оповещающая о невозможности корректно разобрать строку xml-элемента.
 */
public class XmlElementParseException extends XmlParseException{

    //TODO добавить курсор
    public XmlElementParseException(String message) {
        super(message);
    }

    public XmlElementParseException(String message, CursorPosition cursorPosition) {
        super(message, cursorPosition);
    }
}
