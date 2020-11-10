package com.bolingcavalry;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-11-10 20:32
 * @description 常用工具类
 */
public class Utils {

    /**
     * 时间格式化
     * @param timeStamp
     * @return
     */
    public static String time(long timeStamp) {
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(timeStamp));
    }

}
