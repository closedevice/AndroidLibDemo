package com.sbbic.net;

import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by God on 2016/2/29.
 */
public class DefaultThreadPool {

    private static final int COUNT_PROCESSOR = Runtime.getRuntime().availableProcessors();
    static final int BLOCKING_QUEUE_SIZE = 25;
    static final int THREAD_POOL_MAX_SIZE = COUNT_PROCESSOR * 2 + 1;
    static final int THREAD_POOL_SIZE = COUNT_PROCESSOR + 1;

    static ArrayBlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(
            DefaultThreadPool.BLOCKING_QUEUE_SIZE);

    private static DefaultThreadPool instance = null;
    static AbstractExecutorService pool = new ThreadPoolExecutor(
            DefaultThreadPool.THREAD_POOL_SIZE,
            DefaultThreadPool.THREAD_POOL_MAX_SIZE, 15L, TimeUnit.SECONDS,
            DefaultThreadPool.blockingQueue,
            new ThreadPoolExecutor.DiscardOldestPolicy());

    public static synchronized DefaultThreadPool getInstance() {
        if (DefaultThreadPool.instance == null) {
            DefaultThreadPool.instance = new DefaultThreadPool();
        }
        return DefaultThreadPool.instance;
    }

    public static void removeAllTask() {
        DefaultThreadPool.blockingQueue.clear();
    }

    public static void removeTaskFromQueue(final Object obj) {
        DefaultThreadPool.blockingQueue.remove(obj);
    }


    public static void shutdown() {
        if (DefaultThreadPool.pool != null) {
            DefaultThreadPool.pool.shutdown();
        }
    }


    public static void shutdownRNow() {
        if (DefaultThreadPool.pool != null) {
            DefaultThreadPool.pool.shutdownNow();
            try {
                DefaultThreadPool.pool.awaitTermination(1,
                        TimeUnit.MICROSECONDS);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void execute(final Runnable r) {
        if (r != null) {
            try {
                DefaultThreadPool.pool.execute(r);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }
}
