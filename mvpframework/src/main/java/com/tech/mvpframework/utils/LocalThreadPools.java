package com.tech.mvpframework.utils;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalThreadPools {
    private static String TAG = LocalThreadPools.class.getSimpleName();

    private static ExecutorService THREAD_POOL_EXECUTOR;

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2;
    private static final int KEEP_ALIVE_SECONDS = 60;
    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<>(8);
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "MangoTask #" + mCount.getAndIncrement());
        }
    };

    private void initThreadPool() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS, TimeUnit.SECONDS,
                sPoolWorkQueue, sThreadFactory, new RejectedHandler()) {
            @Override
            public void execute(Runnable command) {
                super.execute(command);
//                LogPrint.e(TAG, "ActiveCount=" + getActiveCount());
//                LogPrint.e(TAG, "PoolSize=" + getPoolSize());
//                LogPrint.e(TAG, "Queue=" + getQueue().size());
            }
        };
        //允许核心线程空闲超时时被回收
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        THREAD_POOL_EXECUTOR = threadPoolExecutor;
    }

    private class RejectedHandler implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

        }
    }

    private static LocalThreadPools instance;

    private LocalThreadPools() {
        initThreadPool();
    }

    public static LocalThreadPools getInstance() {
        if (instance == null) {
            instance = new LocalThreadPools();
        }
        return instance;
    }

    public void execute(Runnable command) {
        THREAD_POOL_EXECUTOR.execute(command);
    }

    /**
     * 通过interrupt方法尝试停止正在执行的任务，但是不保证真的终止正在执行的任务
     * 停止队列中处于等待的任务的执行
     * 不再接收新的任务
     *
     * @return 等待执行的任务列表
     */
    public List<Runnable> shutdownNow() {
        return THREAD_POOL_EXECUTOR.shutdownNow();
    }

    /**
     * 停止队列中处于等待的任务
     * 不再接收新的任务
     * 已经执行的任务会继续执行
     * 如果任务已经执行完了没有必要再调用这个方法
     */
    public void shutDown() {
        THREAD_POOL_EXECUTOR.shutdown();
        sPoolWorkQueue.clear();
    }
}
