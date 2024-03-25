package app.xml.exception;

import app.xml.CursorPosition;

/**
 * Сигнализирует о том, что достигнут конец файла,
 *      и у всех пройденных узлов в файле считан закрывающий элемент.
 */
public class XmlExpectedEndOfFileException extends XmlParseException {


    public XmlExpectedEndOfFileException(CursorPosition cursorPosition){
        super(cursorPosition);
    }
    public XmlExpectedEndOfFileException(String message, CursorPosition position) {
        super(message, position);
    }
}
