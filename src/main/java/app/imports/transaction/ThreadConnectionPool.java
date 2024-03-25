package app.imports.transaction;

import app.DbConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Пул соединений с БД, где каждое соединение привязано к определённому {@link Thread}.
 * Если у потока ещё нет ассоциированного с ним соединения,
 *      тогда создаётся новое, иначе будет использоваться существующее.
 * После выполнения всех необходимых операций с соединением поток должен самостоятельно закрыть его.
 *
 *`Класс является потоко-безопасным.
 */
public class ThreadConnectionPool implements AutoCloseable {

    private final ConcurrentHashMap<Long, Connection> connectionPool;
    private final DbConnection dbConnection;

    public ThreadConnectionPool(DbConnection dbConnection) {
        this.connectionPool = new ConcurrentHashMap<>();
        this.dbConnection = dbConnection;
    }

    /**
     * Возвращает соединение с БД, ассоциированное с текущим потоком.
     * Если такого соединения, создаётся новое.
     * @return соединение с БД
     * @throws SQLException - если не удается создать соединение
     */
    public Connection getConnection() throws SQLException {
        Long threadId = Thread.currentThread().getId();
        if(connectionPool.containsKey(threadId)){
           return connectionPool.get(threadId);
        }
        Connection conn = dbConnection.getConnection();
        connectionPool.put(threadId, conn);
        return conn;
    }

    /**
     * Закрывает существующее соединение, связанное с текущим потоком.
     * @throws SQLException - если не удаётся закрыть соединение
     * @throws NullPointerException - если у текущего потока нет открытого соединения
     */
    public void closeConnection() throws SQLException {
        Long threadId = Thread.currentThread().getId();
        if(!connectionPool.containsKey(threadId)){
            throw new NullPointerException("Current thread has not any opened connections.");
        }
        connectionPool.get(threadId).close();
        connectionPool.remove(threadId);
    }

    /**
     * Закрывает все соединения в пуле
     * @throws SQLException - если не удается закрыть какое-то из соединений
     */
    @Override
    public void close() throws SQLException {
        for(Map.Entry<Long, Connection> entry : connectionPool.entrySet()){
            entry.getValue().close();
        }
    }

}
