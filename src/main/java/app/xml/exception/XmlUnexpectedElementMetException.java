package app.xml.exception;

import app.xml.CursorPosition;

/**
 * Исключительная ситуация, сигнализирующая, что при парсинге встречен элемент,
 *      которого быть не должно, либо он должен быть в другом месте.
 */
public class XmlUnexpectedElementMetException extends XmlParseException{

    public XmlUnexpectedElementMetException(CursorPosition cursorPosition) {
        super(cursorPosition);
    }

    public XmlUnexpectedElementMetException(String message) {
        super(message);
    }
}
