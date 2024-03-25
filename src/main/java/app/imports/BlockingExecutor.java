package app.imports;

import java.util.concurrent.*;

/**
 * Executor, который блокирует поток, добавляющий новую задачу,
 *      если очередь уже забита.
 */
public class BlockingExecutor extends ThreadPoolExecutor {

    /**
     * Так как у {@link ThreadPoolExecutor} нет подходящего {@link RejectedExecutionHandler}
     *      для такого поведения, используется семафора с кол-вом разрешений равным размеру очереди задач.
     */
    private final Semaphore semaphore;

    public BlockingExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        semaphore = new Semaphore(workQueue.remainingCapacity());
    }

    public BlockingExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        semaphore = new Semaphore(workQueue.remainingCapacity());
    }

    public BlockingExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
        semaphore = new Semaphore(workQueue.remainingCapacity());
    }

    public BlockingExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        semaphore = new Semaphore(workQueue.remainingCapacity());
    }

    /**
     * Добавляет задачу на выполнение.
     * Если очередь задач забита, поток блокируется до освобождения места в очереди.
     * @param callable задача на выполнение
     * @param <T> тип возвращаемого значения
     * @return future
     */
    @Override
    public <T> Future<T> submit(Callable<T> callable){

        try{
            semaphore.acquire();
        } catch (InterruptedException e){
            throw new RuntimeException(e);
        }

        Callable<T> wrappedCall = () -> {
            semaphore.release();
            return callable.call();
        };

        return super.submit(wrappedCall);
    }

}
