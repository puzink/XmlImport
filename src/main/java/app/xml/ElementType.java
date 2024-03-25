package app.xml;

/**
 * Тип элемента.
 * Элемент может быть:
 *  1) открывающим: без специального символа в начале
 *  2) закрывающим: символ '/' в начале
 *  3) прологом: символ '?' в начале.
 */
public enum ElementType {
    OPEN, CLOSE, PROLOG;
}