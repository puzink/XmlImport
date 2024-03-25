package app.imports.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Задача, исполняющаяся в транзакции {@link java.sql.Connection}
 *      с уровнем изоляции {@link Connection#TRANSACTION_SERIALIZABLE}.
 * @param <T> - результат задачи
 * @see TransactionalTask
 */
public abstract class SerializationTransactionTask<T> extends TransactionalTask<T> {

    /**
     * Sql state ошибки транзакции с уровнем изоляции {@link Connection#TRANSACTION_SERIALIZABLE}.
     * При её возникновении можно повторить транзакцию.
     */
    private static final String SERIALIZATION_FAILURE_SQL_STATE = "40001";

    public SerializationTransactionTask(ThreadConnectionTransactionManager tx) {
        super(tx, Connection.TRANSACTION_SERIALIZABLE);
    }

    @Override
    public boolean isRecoverable(SQLException e){
        return Objects.equals(SERIALIZATION_FAILURE_SQL_STATE, e.getSQLState());
    }

}
