package com.bolingcavalry.service.impl;

import io.quarkus.logging.Log;

import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class AccountBalanceService {

    // 账户余额，假设初始值为100
    AtomicInteger accountBalance = new AtomicInteger(100);

    /**
     * 查询余额
     * @return
     */
    public int get() {
        return accountBalance.get();
    }

    /**
     * 充值
     * @param value
     * @throws InterruptedException
     */
    public void deposit(int value) throws InterruptedException {
        Log.infov("start deposit, balance [{0}], deposit value [{1}]", accountBalance.get(), value);
        // 模拟耗时的操作
        Thread.sleep(100);
        Log.infov("end deposit, balance [{0}]",accountBalance.addAndGet(value));
    }

    /**
     * 扣费
     * @param value
     * @throws InterruptedException
     */
    public void deduct(int value) throws InterruptedException {
        Log.infov("start deduct, balance [{0}], deduct value [{1}]", accountBalance.get(), value);
        // 模拟耗时的操作
        Thread.sleep(100);
        Log.infov("end deduct, balance [{0}]",accountBalance.addAndGet(value*-1));
    }

}
