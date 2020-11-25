package com.xiaoyaozi.threadPool.service.impl;

import com.xiaoyaozi.threadPool.service.PriceService;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * tip:
 *
 * @author xiaoyaozi
 * createTime: 2020-11-23 15:58
 */
@Service
public class PriceServiceImpl implements PriceService {
    @Override
    public String info() {
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "info";
    }

}
