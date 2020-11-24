package com.xiaoyaozi.threadPoll;

import cn.hutool.core.thread.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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
    private static final HashMap<String, ThreadPoolInfo> THREAD_POOL_INFO_MAP = new HashMap<>(16);

    static {
        Field tempField = null;
        try {
            tempField = LinkedBlockingQueue.class.getDeclaredField("capacity");
            tempField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        LINKED_CAPACITY_FIELD = tempField;

        initThreadPool(2, 5, 60, TimeUnit.SECONDS, 10, LINKED_THREAD_POLL);
        initThreadPool(2, 5, 60, TimeUnit.SECONDS, 0, SYNC_THREAD_POLL);
    }

    private static void initThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int queueLength, String threadName) {
        BlockingQueue<Runnable> queue = queueLength <= 0 ? new SynchronousQueue<>(true) : new LinkedBlockingQueue<>(queueLength);
        ThreadPoolInfo poolInfo = ThreadPoolInfo.builder().threadPoolName(threadName).build()
                .setQueueType(queueLength <= 0 ? "SynchronousQueue" : "LinkedBlockingQueue")
                .setQueueSize(0).setQueueLength(0).setQueueUsedPercent("-").setRejectCount(0);
        THREAD_POOL_MAP.put(threadName, new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, queue
                , new NamedThreadFactory(threadName, false)));
        THREAD_POOL_INFO_MAP.put(threadName, poolInfo);
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
        try {
            return THREAD_POOL_MAP.get(threadPoolName).submit(callable);
        } catch (Exception e) {
            if (e instanceof RejectedExecutionException) {
                THREAD_POOL_INFO_MAP.get(threadPoolName).setRejectCount(THREAD_POOL_INFO_MAP.get(threadPoolName).getRejectCount() + 1);
                log.error("任务超出线程池队列长度，请注意", e);
            } else {
                log.error("任务执行出现异常");
            }
        }
        return null;
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

    /**
     * tip: 获取所有线程池的信息
     * author: xiaoyaozi
     * createTime: 2020-11-24 21:23
     * @return ThreadPoolInfo> List
     */
    public static List<ThreadPoolInfo> getThreadPoolInfo() {
        for (Map.Entry<String, ThreadPoolExecutor> entry : THREAD_POOL_MAP.entrySet()) {
            ThreadPoolExecutor threadPool = entry.getValue();
            ThreadPoolInfo poolInfo = THREAD_POOL_INFO_MAP.get(entry.getKey());
            poolInfo.setCorePoolSize(threadPool.getCorePoolSize())
                    .setActiveCount(threadPool.getActiveCount())
                    .setMaximumPoolSize(threadPool.getMaximumPoolSize())
                    .setThreadUsedPercent(divide(threadPool.getActiveCount(), threadPool.getMaximumPoolSize()))
                    .setCompletedTaskCount(threadPool.getCompletedTaskCount());
            if (threadPool.getQueue() instanceof LinkedBlockingQueue) {
                // only linked have length
                poolInfo.setQueueLength(getQueueLength((LinkedBlockingQueue<?>) threadPool.getQueue()))
                        .setQueueSize(threadPool.getQueue().size())
                        .setRemainingCapacity(threadPool.getQueue().remainingCapacity())
                        .setQueueUsedPercent(divide(threadPool.getQueue().size(), getQueueLength((LinkedBlockingQueue<?>) threadPool.getQueue())));
            }
        }
        return new ArrayList<>(THREAD_POOL_INFO_MAP.values());
    }

    /**
     * tip: 保留两位小数
     * author: xiaoyaozi
     * createTime: 2020-11-24 22:41
     * @param num1 num1
     * @param num2 num2
     * @return String String
     */
    public static String divide(int num1, int num2) {
        return String.format("%1.2f%%", Double.parseDouble(String.valueOf(num1)) / Double.parseDouble(String.valueOf(num2)) * 100);
    }

}
