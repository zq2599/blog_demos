package com.bolingcavalry;


import com.alibaba.fastjson.JSON;
import org.springframework.util.StopWatch;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 演示StopWatch工具类的用法
 * @email zq2599@gmail.com
 * @Date 2017/10/13 下午9:46
 */
public class StopWatchDemo {

    /**
     * 延时的方法
     * @param time 延时时常，单位毫秒
     */
    private static void delay(long time){
        try{
            Thread.sleep(time);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    /**
     * 假设这个方法在执行本地调用，耗时100毫秒
     */
    private void executeNative(){
        delay(100);
    }

    /**
     * 假设这个方法在执行数据库操作，耗时200毫秒
     */
    private void executeDB(){
        delay(200);
    }

    /**
     * 假设这个方法在执行远程调用，耗时300毫秒
     */
    private void executeRPC(){
        delay(300);
    }

    public static void main(String[] args){

        StopWatchDemo demo = new StopWatchDemo();

        //起个名字，在最后面统计信息中会打印出来
        StopWatch stopWatch = new StopWatch("stopwatch test");

        //记录本地方法的耗时
        stopWatch.start("执行本地方法");
        demo.executeNative();
        stopWatch.stop();

        //记录数据库操作的耗时
        stopWatch.start("执行数据库操作");
        demo.executeDB();
        stopWatch.stop();

        //记录数远程调用耗时
        stopWatch.start("执行远程调用");
        demo.executeRPC();
        stopWatch.stop();

        //打印一份格式化好的汇总统计信息
        System.out.println(stopWatch.prettyPrint());

        System.out.println("\n");

        //打印统计名称和总的耗时
        System.out.println(stopWatch.shortSummary());

        System.out.println("\n");

        //一共执行了三段统计，stopWatch.getTaskInfo()返回的数组中就是每段的信息，这里用fastjson转成字符串便于查看
        System.out.println(JSON.toJSON(stopWatch.getTaskInfo()));

    }



}
