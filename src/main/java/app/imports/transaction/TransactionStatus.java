package app.imports.transaction;

import lombok.Getter;

/**
 * Информация о транзакции.
 */
@Getter
public class TransactionStatus {

    /**
     * Ссылка на транзакцию, в которой начата текущая.
     * Может быть null.
     */
    private TransactionStatus parent = null;

    public TransactionStatus(TransactionStatus parent) {
        this.parent = parent;
    }

}
