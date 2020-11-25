package com.xiaoyaozi.threadPool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * tip: 任务执行时长桶
 *
 * @author xiaoyaozi
 * createTime: 2020-11-25 15:25
 */
@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExecutorTimeBucket {
    /**
     * 桶上限
     */
    private Long upperBound;
    /**
     * 数量
     */
    private Long count;
}
