package app.imports.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Простая реализация {@link ThreadConnectionTransactionManager}.
 */
public class ThreadConnectionTransactionManagerImpl
        implements ThreadConnectionTransactionManager {

    private final ThreadConnectionPool connectionPool;
    private final ConcurrentHashMap<Long, TransactionStatus> transactions = new ConcurrentHashMap<>();

    public ThreadConnectionTransactionManagerImpl(ThreadConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    /**
     * Начинает новую транзакцию.
     * @param isolationLevel - уровень изоляции транзакции
     * @throws SQLException - ошибка при запуске транзакции
     */
    public void begin(int isolationLevel) throws SQLException {
        Long threadId = Thread.currentThread().getId();
        TransactionStatus currentTransaction = transactions.get(threadId);
        TransactionStatus newTransaction = null;
        if (currentTransaction != null) {
            newTransaction = new TransactionStatus(currentTransaction);
        } else {
            Connection connection = connectionPool.getConnection();
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(isolationLevel);
            newTransaction = new TransactionStatus(null);
        }

        transactions.put(threadId, newTransaction);
    }

    /**
     * Фиксирует или закрывает последнюю начатую транзакцию в потоке в зависимости от её статуса.
     * Если эта транзакция является логической,
     *      тогда фиксации не происходит, и она лишь закрывается.
     * @throws SQLException - ошибка при фиксации изменений
     */
    public void commit() throws SQLException {
        Long threadId = Thread.currentThread().getId();
        TransactionStatus lastOpenedTransaction = transactions.get(threadId);
        if (lastOpenedTransaction == null) {
            throw new NullPointerException("Transaction is not exists.");
        }

        if (lastOpenedTransaction.getParent() != null) {
            transactions.put(threadId, lastOpenedTransaction.getParent());
        } else {
            transactions.remove(threadId);
            Connection conn = connectionPool.getConnection();
            conn.commit();
            connectionPool.closeConnection();
        }
    }

    /**
     * Откатывает транзакцию.
     * Если транзакция является логической, тогда происходит откат основной транзакции,
     *      которая породила эту.
     * @throws SQLException - ошибка при откате изменений
     */
    @Override
    public void rollback() throws SQLException {
        Long threadId = Thread.currentThread().getId();
        TransactionStatus transaction = transactions.get(threadId);
        connectionPool.getConnection().rollback();
        if (transaction.getParent() == null) {
            transactions.remove(threadId);
        } else {
            transactions.put(threadId, transaction.getParent());
        }

    }

    /**
     * Закрывает {@link java.sql.Connection}, связанный с транзакцией.
     * @throws SQLException - ошибки при закрытии соединения
     */
    @Override
    public void close() throws SQLException {
        connectionPool.closeConnection();
    }

}

