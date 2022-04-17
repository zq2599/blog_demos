package com.bolingcavalry;

import com.bolingcavalry.service.impl.AccountBalanceService;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.concurrent.CountDownLatch;

@QuarkusTest
public class LockTest {

    @Inject
    AccountBalanceService account;

    @Test
    public void test() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        int initValue = account.get();

        final int COUNT = 10;

        // 这是个只负责读取的线程，循环读10次，每读一次就等待50毫秒
        new Thread(() -> {

            for (int i=0;i<COUNT;i++) {
                // 读取账号余额
                Log.infov("current balance {0}", account.get());
            }

            latch.countDown();
        }).start();

        // 这是个存钱的线程，循环存10次，每次存2元
        new Thread(() -> {
            for (int i=0;i<COUNT;i++) {
                account.deposit(2);
            }
            latch.countDown();
        }).start();

        // 这是个取钱的线程，循环取10次，每取1元
        new Thread(() -> {
            for (int i=0;i<COUNT;i++) {
                account.deduct(1);
            }
            latch.countDown();
        }).start();

        latch.await();

        int finalValue = account.get();
        Log.infov("finally, current balance {0}", finalValue);
        Assertions.assertEquals(initValue + COUNT, finalValue);
    }
}
