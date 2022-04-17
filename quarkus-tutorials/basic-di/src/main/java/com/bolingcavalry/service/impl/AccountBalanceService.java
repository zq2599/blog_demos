package com.bolingcavalry.service.impl;

import io.quarkus.arc.Lock;
import io.quarkus.logging.Log;

import javax.enterprise.context.ApplicationScoped;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
@Lock
public class AccountBalanceService {

    // 账户余额，假设初始值为100
    int accountBalance = 100;

    /**
     * 查询余额
     * @return
     */
    @Lock(value = Lock.Type.READ, time = 5, unit = TimeUnit.SECONDS)
    public int get() {
        // 模拟耗时的操作
        try {
            Thread.sleep(80);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return accountBalance;
    }

    /**
     * 模拟了一次充值操作，
     * 将账号余额读取到本地变量，
     * 经过一秒钟的计算后，将计算结果写入账号余额，
     * 这一秒内，如果账号余额发生了变化，就会被此方法的本地变量覆盖，
     * 因此，多线程的时候，如果其他线程修改了余额，那么这里就会覆盖掉，导致多线程同步问题，
     * AccountBalanceService类使用了Lock注解后，执行此方法时，其他线程执行AccountBalanceService的方法时就会block住，避免了多线程同步问题
     * @param value
     * @throws InterruptedException
     */
    public void deposit(int value) {
        // 先将accountBalance的值存入tempValue变量
        int tempValue  = accountBalance;
        Log.infov("start deposit, balance [{0}], deposit value [{1}]", tempValue, value);

        // 模拟耗时的操作
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        tempValue += value;

        // 用tempValue的值覆盖accountBalance，
        // 这个tempValue的值是基于100毫秒前的accountBalance计算出来的，
        // 如果这100毫秒期间其他线程修改了accountBalance，就会导致accountBalance不准确的问题
        // 例如最初有100块，这里存了10块，所以余额变成了110,
        // 但是这期间如果另一线程取了5块，那余额应该是100-5+10=105，但是这里并没有靠拢100-5，而是很暴力的将110写入到accountBalance
        accountBalance = tempValue;

        Log.infov("end deposit, balance [{0}]", tempValue);
    }

    /**
     * 模拟了一次扣费操作，
     * 将账号余额读取到本地变量，
     * 经过一秒钟的计算后，将计算结果写入账号余额，
     * 这一秒内，如果账号余额发生了变化，就会被此方法的本地变量覆盖，
     * 因此，多线程的时候，如果其他线程修改了余额，那么这里就会覆盖掉，导致多线程同步问题，
     * AccountBalanceService类使用了Lock注解后，执行此方法时，其他线程执行AccountBalanceService的方法时就会block住，避免了多线程同步问题
     * @param value
     * @throws InterruptedException
     */
    public void deduct(int value) {
        // 先将accountBalance的值存入tempValue变量
        int tempValue  = accountBalance;
        Log.infov("start deduct, balance [{0}], deposit value [{1}]", tempValue, value);

        // 模拟耗时的操作
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        tempValue -= value;

        // 用tempValue的值覆盖accountBalance，
        // 这个tempValue的值是基于100毫秒前的accountBalance计算出来的，
        // 如果这100毫秒期间其他线程修改了accountBalance，就会导致accountBalance不准确的问题
        // 例如最初有100块，这里存了10块，所以余额变成了110,
        // 但是这期间如果另一线程取了5块，那余额应该是100-5+10=105，但是这里并没有靠拢100-5，而是很暴力的将110写入到accountBalance
        accountBalance = tempValue;

        Log.infov("end deduct, balance [{0}]", tempValue);
    }

}
