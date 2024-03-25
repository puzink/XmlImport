package app.imports;

/**
 * Сигнализирует о возникновении ошибки при импорте данных из xml-файла.
 */
public class XmlImportException extends Exception{

    public XmlImportException(String message){
        super(message);
    }

}
