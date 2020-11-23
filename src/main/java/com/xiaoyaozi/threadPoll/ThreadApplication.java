package com.xiaoyaozi.threadPoll;

import com.xiaoyaozi.threadPoll.service.GoodsService;
import com.xiaoyaozi.threadPoll.service.PriceService;
import com.xiaoyaozi.threadPoll.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

    @GetMapping("/goods")
    public String goods() {
        Future<String> goodFuture = ThreadUtils.pushTaskToThreadPollQueue(ThreadUtils.LINKED_THREAD_POLL, () -> goodsService.info());
        Future<String> priceFuture = ThreadUtils.pushTaskToThreadPollQueue(ThreadUtils.LINKED_THREAD_POLL, () -> priceService.info());
        Future<String> stockFuture = ThreadUtils.pushTaskToThreadPollQueue(ThreadUtils.LINKED_THREAD_POLL, () -> stockService.info());
        try {
            return goodFuture.get() + priceFuture.get() + stockFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("线程池获取放回结果失败", e);
            return "error";
        }
    }
}
