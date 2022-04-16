package com.bolingcavalry;

import com.bolingcavalry.annonation.MyQualifier;
import com.bolingcavalry.service.HelloQualifier;
import com.bolingcavalry.service.impl.AccountBalanceService;
import com.bolingcavalry.service.impl.HelloQualifierA;
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
    public void test() {
        CountDownLatch latch = new CountDownLatch(3);

        final int COUNT = 10;

        // 这是个只负责读取的线程，循环读10次，每读一次就等待50毫秒
        new Thread(() -> {

            for (int i=0;i<COUNT;i++) {
                // 读取账号余额
                Log.infov("current balance {0}", account);

                // 等待50毫秒
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            latch.countDown();
        }).start();


        // 这是个存钱的线程，循环存10次，每次存2元
        new Thread(() -> {
            for (int i=0;i<COUNT;i++) {
                account.deposit(1);
            }
            latch.countDown();
        }).start();

        // 这是个取钱的线程，循环取10次，每取1元
        new Thread(() -> {
            for (int i=0;i<COUNT;i++) {
                account.deposit(1);
            }
            latch.countDown();
        }).start();

        Log.infov("finally, current balance {0}", account);
    }
}
