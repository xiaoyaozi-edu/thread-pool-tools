package com.xiaoyaozi.threadPoll;

import cn.hutool.core.thread.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 * tip:
 *
 * @author xiaoyaozi
 * createTime: 2020-11-23 09:27
 */
@Slf4j
public class ThreadUtils {

    public static final String LINKED_THREAD_POLL = "linked_thread_poll";
    public static final String SYNC_THREAD_POLL = "sync_thread_poll";
    /**
     * 放射capacity，用于动态修改linked的length
     */
    private static final Field LINKED_CAPACITY_FIELD;
    /**
     * 存放线程池信息
     */
    private static final HashMap<String, ThreadPoolExecutor> THREAD_POOL_MAP = new HashMap<>(16);

    static {
        Field tempField = null;
        try {
            tempField = LinkedBlockingQueue.class.getDeclaredField("capacity");
            tempField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        LINKED_CAPACITY_FIELD = tempField;
        ThreadPoolExecutor linkedThreadPoll = new ThreadPoolExecutor(2, 5, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100), new NamedThreadFactory(LINKED_THREAD_POLL, false));
        ThreadPoolExecutor syncThreadPoll = new ThreadPoolExecutor(2, 5, 60, TimeUnit.SECONDS,
                new SynchronousQueue<>(true), new NamedThreadFactory(SYNC_THREAD_POLL, false));
        THREAD_POOL_MAP.put(LINKED_THREAD_POLL, linkedThreadPoll);
        THREAD_POOL_MAP.put(SYNC_THREAD_POLL, syncThreadPoll);
    }

    /**
     * tip: 推送任务到线程池队列中
     * author: xiaoyaozi
     * createTime: 2020-11-23 18:01
     * @param threadPoolName 线程池名
     * @param callable 任务
     * @return Future<T> 返回值
     */
    public static <T> Future<T> pushTaskToThreadPollQueue(String threadPoolName, Callable<T> callable) {
        if (!THREAD_POOL_MAP.containsKey(threadPoolName)) {
            throw new IllegalArgumentException();
        }
        return THREAD_POOL_MAP.get(threadPoolName).submit(callable);
    }

    /**
     * tip: 动态修改线程池信息
     * author: xiaoyaozi
     * createTime: 2020-11-23 18:14
     * @param poolInfo 线程次信息
     */
    public static void dynamicModifyThreadPool(ThreadPoolInfo poolInfo) {
        if (!THREAD_POOL_MAP.containsKey(poolInfo.getThreadPoolName())) {
            throw new IllegalArgumentException();
        }
        ThreadPoolExecutor threadPool = THREAD_POOL_MAP.get(poolInfo.getThreadPoolName());
        if (poolInfo.getCorePoolSize() != null && poolInfo.getCorePoolSize() > 0) {
            threadPool.setCorePoolSize(poolInfo.getCorePoolSize());
        }
        if (poolInfo.getMaximumPoolSize() != null && poolInfo.getMaximumPoolSize() > 0) {
            threadPool.setMaximumPoolSize(poolInfo.getMaximumPoolSize());
        }
        if (poolInfo.getQueueLength() != null && poolInfo.getQueueLength() > 0 && threadPool.getQueue() instanceof LinkedBlockingQueue) {
            setQueueLength((LinkedBlockingQueue<?>) threadPool.getQueue(), poolInfo.getQueueLength());
        }
    }

    /**
     * tip: 利用反射获取queue里的length
     * author: xiaoyaozi
     * createTime: 2020-11-23 18:24
     * @param queue queue
     * @return int length
     */
    public static int getQueueLength(LinkedBlockingQueue<?> queue) {
        try {
            return (int) LINKED_CAPACITY_FIELD.get(queue);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * tip: 利用反射修改queue里的length
     * author: xiaoyaozi
     * createTime: 2020-11-23 18:21
     * @param queue queue
     * @param length length
     */
    public static void setQueueLength(LinkedBlockingQueue<?> queue, int length) {
        try {
            LINKED_CAPACITY_FIELD.set(queue, length);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static ThreadPoolExecutor buildThreadPoolExecutor() {
        return new ThreadPoolExecutor(2, 5, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10), new NamedThreadFactory("xiaoyaozi_", false));
    }

//    public static void start() {
//        for (int i = 0; i < 15; i++) {
//            THREAD_POOL.submit(() -> {
//                showThreadPoolInfo(THREAD_POOL, "创建任务");
//                try {
//                    TimeUnit.SECONDS.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            });
//        }
//    }
//
//    public static void push() {
//        try {
//            THREAD_POOL.submit(() -> {
//                showThreadPoolInfo(THREAD_POOL, "添加任务");
//                try {
//                    TimeUnit.SECONDS.sleep(10);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            });
//        } catch (Exception exception) {
//            log.error("任务超出线程池队列长度，请注意", exception);
//        }
//    }
//
//    public static void modify(Integer queueLength) {
//        setQueueLength((LinkedBlockingQueue<?>) THREAD_POOL.getQueue(), queueLength);
//        showThreadPoolInfo(THREAD_POOL, "动态修改后的线程池信息");
//    }

    public static void dynamicModifyThreadPool() {
        ThreadPoolExecutor threadPool = buildThreadPoolExecutor();
        for (int i = 0; i < 20; i++) {
            threadPool.submit(() -> {
                showThreadPoolInfo(threadPool, "创建任务");
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        Future<String> submit = threadPool.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return null;
            }
        });

        showThreadPoolInfo(threadPool, "设置队列长度");
        setQueueLength((LinkedBlockingQueue<?>) threadPool.getQueue(),100);

        for (int i = 0; i < 20; i++) {
            threadPool.submit(() -> {
                showThreadPoolInfo(threadPool, "创建任务");
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
//
//        threadPool.setCorePoolSize(10);
//        threadPool.setMaximumPoolSize(10);
//        threadPool.prestartAllCoreThreads();
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void showThreadPoolInfo(ThreadPoolExecutor threadPool, String msg) {
        LinkedBlockingQueue<?> queue = (LinkedBlockingQueue<?>) threadPool.getQueue();
        int queueLength = getQueueLength(queue);
        System.out.println(Thread.currentThread().getName() + "-" + msg + "-:"
                + "corePoolSize: " + threadPool.getCorePoolSize()
                + ", activeCount: " + threadPool.getActiveCount()
                + ", maximumPoolSize: " + threadPool.getMaximumPoolSize()
                + ", threadUsedPercent: " + divide(threadPool.getActiveCount(), threadPool.getMaximumPoolSize())
                + ", completedTaskCount: " + threadPool.getCompletedTaskCount()
                + ", queueLength: " + queueLength
                + ", queueSize: " + queue.size()
                + ", remainingCapacity: " + queue.remainingCapacity()
                + ", queueUsedPercent: " + divide(queue.size(), queueLength));
    }




    public static String divide(int num1, int num2) {
        return String.format("%1.2f%%", Double.parseDouble(String.valueOf(num1)) / Double.parseDouble(String.valueOf(num2)) * 100);
    }



}
