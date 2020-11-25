package com.xiaoyaozi.threadPool;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * tip: 可以记录任务执行时长的callable
 *
 * @author xiaoyaozi
 * createTime: 2020-11-25 16:00
 */
public class ExecutorTimeTask<V> extends FutureTask<V> {

    public ExecutorTimeTask(Callable<V> callable) {
        super(callable);
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        super.run();
        // statistics executor time
        ExecutorTimeUtils.statisticsExecutorTime(Thread.currentThread().getName(), start);
    }
}
