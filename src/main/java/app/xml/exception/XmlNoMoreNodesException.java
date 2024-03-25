package app.xml.exception;

import app.xml.CursorPosition;

import java.io.IOException;

/**
 * В xml-файле больше нет узлов.
 */
public class XmlNoMoreNodesException extends XmlParseException{

    public XmlNoMoreNodesException(CursorPosition cursorPosition){
        this(null, cursorPosition);
    }

    public XmlNoMoreNodesException(String message, CursorPosition position) {
        super(message, position);
    }
}
