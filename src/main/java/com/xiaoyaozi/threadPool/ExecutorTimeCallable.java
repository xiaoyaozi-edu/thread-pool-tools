package com.xiaoyaozi.threadPool;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * tip: 可以记录任务执行时长的callable
 *
 * @author xiaoyaozi
 * createTime: 2020-11-25 16:00
 */
public class ExecutorTimeCallable<V> implements Callable<V> {

    /** The underlying callable; nulled out after running */
    private Callable<V> callable;
    /**
     * threadPoolName
     */
    private String threadPoolName;

    public ExecutorTimeCallable(String threadPoolName, Callable<V> callable) {
        if (callable == null) {
            throw new NullPointerException();
        }
        this.threadPoolName = threadPoolName;
        this.callable = callable;
    }

    @Override
    public V call() throws Exception {
        long start = System.currentTimeMillis();
        V call = this.callable.call();
        // statistics executor time
        ExecutorTimeUtils.statisticsExecutorTime(threadPoolName, start);
        return call;
    }
}
