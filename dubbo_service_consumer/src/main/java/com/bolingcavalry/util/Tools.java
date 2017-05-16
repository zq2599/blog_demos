package com.bolingcavalry.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 常用工具类
 * @email zq2599@gmail.com
 * @Date 17/5/16 下午1:39
 */
public class Tools {

    /**
     * 从HttpServletRequest中获取字符串，再转成int型
     * @param request
     * @param key
     * @param defaultVal
     * @return
     */
    public static int getInt(HttpServletRequest request, String key, int defaultVal){
        if(null==request || StringUtils.isBlank(key)){
            return defaultVal;
        }

        String raw = request.getParameter(key);

        if(StringUtils.isNumeric(raw)){
            return Integer.valueOf(raw);
        }

        return defaultVal;
    }

}
