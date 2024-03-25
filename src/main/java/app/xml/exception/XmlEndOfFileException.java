package app.xml.exception;

import app.xml.CursorPosition;

/**
 * Достигнут конец файла, но не пройдено дерево узлов(некоторые узлы не закрыты).
 */
public class XmlEndOfFileException extends XmlParseException{

    private static String message = "Reached the end of the file.";

    public XmlEndOfFileException(CursorPosition cursorPosition) {
        super(cursorPosition);
    }
}
