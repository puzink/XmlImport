package app.xml.exception;

import app.xml.CursorPosition;

import java.io.IOException;


/**
 * Исключительная ситуация, возникающая при парсинге xml-файла.
 */
public class XmlParseException extends IOException {

    /**
     * Позиция в файле.
     */
    private CursorPosition position;

    public XmlParseException(String message){
        super(message);
    }

    public XmlParseException(CursorPosition cursorPosition){
        this(null, cursorPosition);
    }

    public XmlParseException(String message, CursorPosition position) {
        super(message);
        this.position = position;
    }

}
