package io.bytegate;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolManager {

    public static final int DEFAULT_POOL_SIZE = 20;
    public static final int DEFAULT_QUEUE_SIZE = 100;
    public static final int MAX_POOL_SIZE = 30;
    public static final long KEEP_ALIVE_TIME = 10L;

    private final ExecutorService executorService;

    private ThreadPoolManager(int poolSize) {
        this.executorService = new ThreadPoolExecutor(
                Math.min(poolSize, DEFAULT_POOL_SIZE),
                MAX_POOL_SIZE, KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(DEFAULT_QUEUE_SIZE),
                initThreadFactory()
        );

    }

    private static ThreadFactory initThreadFactory() {
        AtomicInteger counter = new AtomicInteger(1);
        return r -> {
            Thread t = new Thread(r);
            t.setName("bytegate-worker-" + counter.getAndIncrement());
            return t;
        };
    }

    public static ThreadPoolManager create(int poolSize) {
        return new ThreadPoolManager(poolSize);
    }

    public boolean submitTask(Runnable task) {
        try {
            executorService.submit(task);
            return true;
        } catch (RejectedExecutionException e) {
            return false;
        }
    }

    public void close() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }


}
