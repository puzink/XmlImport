package app.xml;

import app.table.Row;

import java.io.IOException;

/**
 * Интерфейс для считывания строк(наборов пар ключ-значение {@link Row}) таблицы из xml-файла.
 */
public interface XmlTableReader extends AutoCloseable{

    /**
     * Возвращает узел, соответствующий корневому элементу файла.
     * Статус узла ({@link Node.NodeStatus} может быть любым.
     * @return узел
     * @throws IOException - если произошла ошибка во время парсинга файла, либо при чтении из него
     */
    Node getTable() throws IOException;

    /**
     * Вовзращает следующую строку таблицы, которая может иметь любое кол-во пар ключ-значение(даже 0).
     * Если строк больше нет - возвращается null.
     * @return строка с парами ключ-значение. Null - если строк больше нет
     * @throws IOException - если произошла ошибка во время парсинга файла, либо при чтении из него
     */
    Row readRow() throws IOException;

}
