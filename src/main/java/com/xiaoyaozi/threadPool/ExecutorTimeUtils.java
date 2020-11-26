package com.xiaoyaozi.threadPool;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * tip: 任务执行时长统计类
 *
 * @author xiaoyaozi
 * createTime: 2020-11-25 15:52
 */
public class ExecutorTimeUtils {

    /**
     * 线程池任务执行时长Map
     */
    private static final HashMap<String, List<ExecutorTimeBucket>> EXECUTOR_TIME_INFO_MAP = new HashMap<>(16);

    public static void initExecutorTimeList(String threadPoolName) {
        // 这里逆序，方便后续添加数量
        int[] array = {Integer.MAX_VALUE, 10000, 5000, 2500, 1000, 500, 250, 100, 50};
        List<ExecutorTimeBucket> collect = Arrays.stream(array).mapToObj(m -> ExecutorTimeBucket.builder().upperBound(m).count((long) 0).build()).collect(Collectors.toList());
        EXECUTOR_TIME_INFO_MAP.put(threadPoolName, collect);
    }

    /**
     * tip: 统计线程任务的执行时长
     * author: xiaoyaozi
     * createTime: 2020-11-25 15:58
     * @param threadPoolName threadPoolName
     * @param start start
     */
    public static void statisticsExecutorTime(String threadPoolName, long start) {
        long executorTime = System.currentTimeMillis() - start;
        for (ExecutorTimeBucket timeBucket : EXECUTOR_TIME_INFO_MAP.get(threadPoolName)) {
            if (executorTime < timeBucket.getUpperBound()) {
                // 落入桶中，继续，否则直接退出即可
                timeBucket.setCount(timeBucket.getCount() + 1);
            } else {
                break;
            }
        }
    }

    /**
     * tip: 查看tpxx下的执行时长
     * author: xiaoyaozi
     * createTime: 2020-11-26 23:54
     * @param tp ls90
     * @return Integer
     */
    public static Integer executorTimeQuantile(String threadPoolName, float tp) {
        if (tp < 0 || tp > 1) {
            return Integer.MAX_VALUE;
        }
        List<ExecutorTimeBucket> timeBuckets = EXECUTOR_TIME_INFO_MAP.get(threadPoolName);
        if (timeBuckets.size() < 2) {
            return Integer.MAX_VALUE;
        }
        // totalCount * tp, notice rank in timeBuckets
        float rank = tp * timeBuckets.get(0).getCount();
        int index = 0;
        for (int i = 0; i < timeBuckets.size(); i++) {
            if (timeBuckets.get(i).getCount() >= rank) {
                index = i;
            } else {
                break;
            }
        }
        if (index == 0) {
            return timeBuckets.get(1).getUpperBound();
        }
        Integer bucketStart = 0;
        Integer bucketEnd = timeBuckets.get(index).getUpperBound();
        Long count = timeBuckets.get(index).getCount();
        if (index < timeBuckets.size() - 1) {
            bucketStart = timeBuckets.get(index + 1).getUpperBound();
            count -= timeBuckets.get(index + 1).getCount();
            rank -= timeBuckets.get(index + 1).getCount();
        }
        return bucketStart + (int) ((bucketEnd - bucketStart) * (rank / count));
    }

    public static List<ExecutorTimeBucket> getExecutorTimeInfo(String threadPoolName) {
        return EXECUTOR_TIME_INFO_MAP.get(threadPoolName);
    }
}
