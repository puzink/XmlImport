package app.imports.transaction;

import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.util.concurrent.Callable;

/**
 * Задача, исполняющаяся в транзакции {@link java.sql.Connection}.
 * @param <T> - результат задачи
 */
public abstract class TransactionalTask<T> implements Callable<T> {

    private static final int DEFAULT_ATTEMPTS_COUNT = 100;

    /**
     * Управляет транзакцией.
     */
    private final ThreadConnectionTransactionManager tx;
    /**
     * уровень изоляции транзакции
     */
    private final int isolationLevel;

    public TransactionalTask(ThreadConnectionTransactionManager tx, int isolationLevel) {
        this.tx = tx;
        this.isolationLevel = isolationLevel;
    }

    /**
     * Пытается выполнить задачу в транзакции с указанным уровнем изоляции.
     * Если во время выполнения задачи проиходит ошибка,
     *      при которой нужно повторить транзакцию, задача повторяется в новой транзакции.
     * Если достигнут предел повторений {@link TransactionalTask#DEFAULT_ATTEMPTS_COUNT},
     *              тогда выбрасывается исключение.
     * @return результат выполнения задачи
     * @throws SQLException - если превышено количество попыток зафиксировать транзакцию
     * @throws Exception - если возникла ошибка при выполении задачи
     */
    @Override
    public T call() throws Exception {
        T res = null;
        long attempts = 0;
        try {
            while (attempts < DEFAULT_ATTEMPTS_COUNT) {
                try {
                    tx.begin(isolationLevel);
                    res = callTask();
                    tx.commit();
                    afterCommit(res);
                    return res;
                } catch (SQLException e) {
                    if(e instanceof SQLRecoverableException
                            || isRecoverable(e)){
                        tx.rollback();
                    } else{
                        throw e;
                    }
                }
                attempts++;
            }
            throw new SQLException("The limit of attempts to commit a transaction has been exceeded");
        } finally {
            tx.close();
        }
    }

    /**
     * Выполняет задачу.
     * @return результат, возвращаемый задачей
     * @throws Exception - ошибка при выполнении задачи
     */
    public abstract T callTask() throws Exception;

    /**
     * Метод вызывается после подтверждения транзакции.
     * @param taskResult - результат задачи
     */
    public abstract void afterCommit(T taskResult);

    /**
     * Проверяет возможность повторить транзакцию.
     * @param e - возникшая во время транзакции ошибка
     * @return true - можно повторить, иначе - false.
     */
    public abstract boolean isRecoverable(SQLException e);
}
