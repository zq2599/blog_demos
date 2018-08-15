package com.bolingcavalry.customizeapplicationevent.util;

/**
 * @Description :
 * @Author : qin_zhao@kingdee.com
 * @Date : 2018-08-15 15:21
 */
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description : 提供一些常用的工具方法
 * @Author : zq2599@gmail.com
 * @Date : 2018-08-14 05:51
 */
public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    /**
     * 打印当前线程堆栈信息
     * @param prefix
     */
    public static void printTrack(String prefix){
        StackTraceElement[] st = Thread.currentThread().getStackTrace();

        if(null==st){
            logger.info("invalid stack");
            return;
        }

        StringBuffer sbf =new StringBuffer();

        for(StackTraceElement e:st){
            if(sbf.length()>0){
                sbf.append(" <- ");
                sbf.append(System.getProperty("line.separator"));
            }

            sbf.append(java.text.MessageFormat.format("{0}.{1}() {2}"
                    ,e.getClassName()
                    ,e.getMethodName()
                    ,e.getLineNumber()));
        }

        logger.info(prefix
                + "\n************************************************************\n"
                + sbf.toString()
                + "\n************************************************************");
    }
}