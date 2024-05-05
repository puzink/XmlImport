package app.xml;

import app.xml.exception.*;

import java.io.*;
import java.nio.charset.StandardCharsets;


/**
 * !!! Важно: парсер не работает с xml-файлами, в которых есть пролог.
 *
 * Реализация xml-парсера, считывающий узлы ({@link app.xml.Node}) из файла по требованию.
 * Такая реализация позволяет работать с файлами,
 * размер которых превышает несколько гигабайт и не помещаются в оперативную память.
 * Для достижения этого парсер считывает символы из файла лишь
 * до появления открывающего элемента({@link Element}) и возвращает узел дерева,
 * тело которого ещё не прочитано.
 * Помимо этого для экономии потребляемой памяти парсер "забывает" все узлы дерева,
 * закрывающие элементы которых были прочитаны, и не записывает символы отступа{@link Character#isWhitespace(char)}
 * в тело узла, пока не будет найден первый символ, не являющийся отступом.
 *
 * В случае возникновения ошибки во время работы парсера и
 * продолжения работы с ним будет воссоздаваться первая встречанная ошибка.
 */

public class XmlLazyParser implements XmlParser{

    /**
     * Xml-файл.
     */
    private final File file;

    /**
     * Буффер для считываемого файла.
     */
    private final BufferedReader buffIn;

    /**
     * Отдельный парсер для компонентов тэга({@link Element}) узла: имени, атрибутов.
     */
    private XmlElementParser elementParser;

    /**
     * Последовательность открытых узлов(текущая ветвь дерева).
     */
    private NodePath nodePath = new NodePath();
    /**
     * Флаг: true - найден ли корневой узел дерева.
     * Он необходим для определения наличия нескольких корневых узлов в файле и их отсутствия(пустой файл).
     */
    private boolean rootElementIsFound;

    /**
     * Содержит ссылку на следующий узел в дереве, который необходимо вернуть.
     * Он необходим для проверки наличия следующего узла в дереве.
     */
    private Node nextNode;

    /**
     * Текущая позиция в файле. Необходим для указания местоположения ошибки в файле.
     */
    //TODO add cursor
    private CursorPosition cursor = new CursorPosition();

    /**
     * При возникновении ошибки во время парскинга, необходимо сохранить экзепляр ошибки,
     * чтобы снова его выкидывать в случае вызова методов парсера.
     */
    private IOException thrownException = null;

    /**
     * Сохраняет значения полей, открывает поток на чтение из файла и
     * считывает открывающий элемент корневого узла.
     * @param file - xml-файл
     * @param elementParser - парсер для тэгов
     * @throws XmlParseException - если файл пустой либо нарушена структура xml-файла
     * @throws IOException - если возникла ошибка во время чтения файла
     */
    public XmlLazyParser(File file, XmlElementParser elementParser) throws IOException{
        this.file = file;
        this.elementParser = elementParser;

//        FileInputStream fileInput = new FileInputStream(file);
//        FileChannel channel =fileInput.getChannel();
        FileReader reader = new FileReader(file, StandardCharsets.UTF_8);
        buffIn = new BufferedReader(reader);

        nextNode = findNextNode();
        rootElementIsFound = true;
    }

    /**
     * Возвращает следующий узел в дереве и считывает данные в файле до нового открывающего элемента узла.
     * Если до вызова метода произошла какая-то ошибка {@link IOException} или {@link XmlParseException},
     * но не {@link XmlExpectedEndOfFileException}, тогда повторно вызывается это исключение.
     * @return следующий узел в дереве
     * @throws IOException - если произошла ошибка во время чтения файла
     * @throws XmlNoMoreNodesException - если в дереве узлов больше нет непосещенных узлов
     * @throws XmlParseException - если произошла ошибка во время парсинга файла
     */
    @Override
    public Node getNextNode() throws IOException{
        if(thrownException != null){
            throw thrownException;
        }
        if(!hasNextNode()){
            thrownException = new XmlNoMoreNodesException("There is no more nodes in the file.", cursor);
            throw thrownException;
        }

        Node currentNode = nextNode;
        try{
            nextNode = findNextNode();
        } catch (XmlExpectedEndOfFileException e){
            nextNode = null;
        }
        return currentNode;

    }

    /**
     * Возвращает true, если в дереве ещё есть непрочитанные узлы.
     * Если до вызова метода произошла ошибка {@link IOException} или {@link XmlParseException},
     * но не {@link XmlExpectedEndOfFileException} и {@link XmlNoMoreNodesException},
     * тогда повторно вызывается это исключение.
     * @return {@code true} - в дереве есть непрочитанные узлы. Иначе - {@code false}.
     * @throws IOException - если произошла ошибка во время чтения файла
     * @throws XmlParseException - если произошла ошибка во время парсинга файла
     */
    @Override
    public boolean hasNextNode() throws IOException {
        if(thrownException instanceof XmlNoMoreNodesException
                || thrownException instanceof XmlExpectedEndOfFileException){
            return false;
        }
        if(thrownException != null){
            throw thrownException;
        }
        return nextNode != null;
    }

    /**
     * Считывает данные из файла, пока не будет найден открывающий элемент следующего узла.
     * Если во время чтения встречаются закрывающие элементы - узлы удаляются из {@link NodePath}.
     * Если встречаются символы, тогда они добавляются в тело последнего открытого узла
     * {@link NodePath#appendIntoBody(char)}.
     * @return следующий узел в дереве
     * @throws IOException - если произошла ошибка во время чтения файла
     * @throws XmlParseException - если произошла ошибка во время парсинга файла
     */
    private Node findNextNode() throws IOException{
        Element element = getNextElement();

        if(existMultiplyRoots()){
            thrownException = new XmlParseException("Multiply root elements.", cursor);
            throw thrownException;
        }
        if(element.isClose()){
            if(!checkNodeClose(element)){
                thrownException = new XmlParseException(
                        "Close element name does not equal to the current node one.",
                        cursor
                );
                throw thrownException;
            }
            nodePath.getTailNode().setStatus(Node.NodeStatus.CLOSED);
            nodePath = nodePath.removeLast();
            return findNextNode();
        }

        Node newNode = new Node(nodePath.getTailNode(), element, Node.NodeStatus.OPENED);
        nodePath = nodePath.addNode(newNode);
        return newNode;
    }


    /**
     * Проверяет на наличие нескольких корневых узлов.
     * @return {@code true} - есть, иначе - {@code false}.
     */
    private boolean existMultiplyRoots() throws IOException {
        return rootElementIsFound && nodePath.isEmpty();
    }


    /**
     * Считывает данные из файла, пока не встретится элемент.
     * @return следующий элемент
     * @throws IOException - если произошла ошибка во время чтения файла
     * @throws XmlParseException - если произошла ошибка во время парсинга файла
     * @see Element
     */
    private Element getNextElement() throws IOException {
        readCharsBeforeNextElement();
        String element = readElement();
        return elementParser.parseElement(element);
    }

    /**
     * Считывает данные до открывающего тэга('<'). Встречаемые символы добавляются в тело узла.
     * @throws IOException - если произошла ошибка во время чтения файла
     * @throws XmlParseException - если файл пустой, завершился до прохода всего дерева
     * @throws XmlUnexpectedSymbolMetException - встретился не символ-отступ, до обнаружения корневого узла.
     * @throws XmlExpectedEndOfFileException - если файл завершился, после прохода всего дерева
     */
    private void readCharsBeforeNextElement() throws IOException {
        int c;
        while((c = buffIn.read()) != '<'){

            checkCorrectionOfFileEnd(c);

            char ch = (char) c;
            if(nodePath.isEmpty() && !Character.isWhitespace(ch)){
                thrownException = new XmlUnexpectedSymbolMetException(ch, cursor);
                throw thrownException;
            }

            if(!nodePath.isEmpty() &&
                    (!nodePath.getTailNode().isBodyEmpty()
                    || !Character.isWhitespace(ch))){
                nodePath.appendIntoBody(ch);
            }
        }
    }

    /**
     * Проверяет на корректность закрытия файла.
     * @throws XmlParseException - если достигнут конец файла во время прохода дерева узлов, либо файл пуст.
     * @throws XmlExpectedEndOfFileException - если дерево узлов пройдено, и достигнут конец файла
     */
    private void checkCorrectionOfFileEnd(int c) throws XmlParseException {
        if(c != -1){
            return;
        }
        if(!rootElementIsFound){
            thrownException = new XmlParseException("File is empty.", cursor);
            throw (XmlParseException) thrownException;
        }
        if(!nodePath.isEmpty()){
            thrownException = new XmlParseException("Xml file closed before end.", cursor);
            throw (XmlParseException) thrownException;
        }

        throw new XmlExpectedEndOfFileException(cursor);
    }

    /**
     * Считывает элемент полностью до появления закрывающего тэга('>').
     * @return содержимое элемента
     * @throws IOException - если произошла ошибка во время чтения данных из файла
     * @throws XmlUnexpectedSymbolMetException - если найден открывающий тэг('<')
     * @throws  XmlParseException - если достигнут конец файла
     */
    private String readElement() throws IOException {
        StringBuilder element = new StringBuilder();
        int c;
        while((c = buffIn.read()) != '>'){
            if(c == -1){
                thrownException = new XmlParseException("Unexpected file end.", cursor);
                throw thrownException;
            }
            char ch = (char) c;
            if(ch == '<'){
                thrownException = new XmlUnexpectedSymbolMetException("Double open tag.", cursor);
                throw thrownException;
            }
            element.append(ch);
        }
        return element.toString();
    }

    /**
     * Проверяет, является ли элемент закрывающим для последнего открытого узла.
     * @param element - закрывающий элемент
     * @return true - является закрывающим элементом для последнего открытого узла.
     *          false - если элемент не является закрывающим
     */
    private boolean checkNodeClose(Element element){
        if(element.getType() != ElementType.CLOSE){
            return false;
        }
        String elementName = element.getName().trim();
        return !nodePath.isEmpty() && elementName.equals(nodePath.getTailNode().getName());
    }

    /**
     * Закрывает буффер.
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        buffIn.close();
    }


}
