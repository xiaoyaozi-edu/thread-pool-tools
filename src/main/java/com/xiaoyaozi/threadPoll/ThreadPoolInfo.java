package com.xiaoyaozi.threadPoll;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * tip: 线程池信息
 *
 * @author xiaoyaozi
 * createTime: 2020-11-23 18:06
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ThreadPoolInfo implements Serializable {

    private static final long serialVersionUID = 398265347285559234L;
    /**
     * 线程池名称（此名称不可以重复）
     */
    private String threadPoolName;
    /**
     * 核心线程数
     */
    private Integer corePoolSize;
    /**
     * 活跃线程
     */
    private Integer activeCount;
    /**
     * 最大线程数
     */
    private Integer maximumPoolSize;
    /**
     * 线程利用率：活跃线程 / 最大线程数
     */
    private String threadUsedPercent;
    /**
     * 已完成任务数（这个数是个近似值，只会大，不会小）
     */
    private Long completedTaskCount;
    /**
     * 队列总长度
     */
    private Integer queueLength;
    /**
     * 队列已用长度
     */
    private Integer queueSize;
    /**
     * 队列可用长度
     */
    private Integer remainingCapacity;
    /**
     * 队列利用率：队列可用长度 / 队列总长度
     */
    private String queueUsedPercent;
    /**
     * 线程池阻塞抛出异常次数
     */
    private Integer rejectCount;
    /**
     * 队列类型
     */
    private String queueType;
}
