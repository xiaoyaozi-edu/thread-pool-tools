package com.xiaoyaozi.threadPool;

import com.xiaoyaozi.threadPool.service.GoodsService;
import com.xiaoyaozi.threadPool.service.PriceService;
import com.xiaoyaozi.threadPool.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * tip:
 *
 * @author xiaoyaozi
 * createTime: 2020-11-23 14:21
 */
@Slf4j
@RestController
@SpringBootApplication
public class ThreadApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThreadApplication.class, args);
    }

    @Autowired
    private GoodsService goodsService;
    @Autowired
    private PriceService priceService;
    @Autowired
    private StockService stockService;


    @GetMapping("/put")
    public List<ThreadPoolInfo> put() {
        ThreadUtils.pushTaskToThreadPoolQueue(ThreadUtils.LINKED_THREAD_POOL, () -> {
            TimeUnit.SECONDS.sleep(1);
            return null;
        });
        return ThreadUtils.getThreadPoolInfo();
    }

    @GetMapping("/iput")
    public List<ExecutorTimeBucket> iput() {
        ThreadUtils.pushTaskToThreadPoolQueue(ThreadUtils.SYNC_THREAD_POOL, () -> {
            TimeUnit.MILLISECONDS.sleep(100);
            return null;
        });
        return ExecutorTimeUtils.getExecutorTimeInfo(ThreadUtils.LINKED_THREAD_POOL);
    }

    @GetMapping("/push")
    public List<ThreadPoolInfo> push() {
        ThreadUtils.pushTaskToThreadPoolQueue(ThreadUtils.LINKED_THREAD_POOL, () -> {
            TimeUnit.SECONDS.sleep(100);
            return null;
        });
        return ThreadUtils.getThreadPoolInfo();
    }


    @GetMapping("/goods")
    public String goods() {
        Future<String> goodFuture = ThreadUtils.pushTaskToThreadPoolQueue(ThreadUtils.LINKED_THREAD_POOL, () -> goodsService.info());
        Future<String> priceFuture = ThreadUtils.pushTaskToThreadPoolQueue(ThreadUtils.LINKED_THREAD_POOL, () -> priceService.info());
        Future<String> stockFuture = ThreadUtils.pushTaskToThreadPoolQueue(ThreadUtils.LINKED_THREAD_POOL, () -> stockService.info());
        try {
            return goodFuture.get() + priceFuture.get() + stockFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("线程池获取回结果失败", e);
            return "error";
        }
    }
}
