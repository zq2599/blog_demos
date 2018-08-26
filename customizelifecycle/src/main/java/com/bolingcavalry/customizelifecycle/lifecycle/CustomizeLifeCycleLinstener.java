package com.bolingcavalry.customizelifecycle.lifecycle;

import com.bolingcavalry.customizelifecycle.util.Utils;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * @Description : SmartLifecycle的实现类，在spring容器初始化完毕和关闭的时候被spring容器回调，完成特定的业务需求
 * @Author : zq2599@gmail.com
 * @Date : 2018-08-25 13:59
 */
@Component
public class CustomizeLifeCycleLinstener implements SmartLifecycle {


    public boolean isRunningFlag() {
        return runningFlag;
    }

    public void setRunningFlag(boolean runningFlag) {
        this.runningFlag = runningFlag;
    }

    private boolean runningFlag = false;

    @Override
    public void stop(Runnable callback) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Utils.printTrack("do stop with callback param");
                //设置为false，表示已经不在执行中了
                setRunningFlag(false);
                //callback中有个CountDownLatch实例，总数是SmartLifecycle对象的数量，
                //此方法被回调时CountDownLatch实例才会减一，初始化容器的线程一直在wait中；
                callback.run();
            }
        }).start();

    }

    @Override
    public void start() {
        Utils.printTrack("do start");
        //设置为false，表示正在执行中
        setRunningFlag(true);
    }

    @Override
    public void stop() {
        Utils.printTrack("do stop");
        //设置为false，表示已经不在执行中了
        setRunningFlag(false);
    }

    @Override
    public int getPhase() {
        return 666;
    }

    @Override
    public boolean isRunning() {
        return isRunningFlag();
    }

    @Override
    public boolean isAutoStartup() {
        //只有设置为true，start方法才会被回调
        return true;
    }
}
