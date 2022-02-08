package com.bolingcavalry.filterdemo;

import nginx.clojure.java.StringFacedJavaBodyFilter;
import java.io.IOException;
import java.util.Map;

/**
 * @author zq2599@sensetime.com
 * @Title: 字符串类型body的filter，修改返回的body内容，将原有的body内容转为全大写
 * @Package
 * @Description:
 * @date 2/8/22 9:28 PM
 */
public class StringFacedUppercaseBodyFilter extends StringFacedJavaBodyFilter {
    @Override
    protected Object[] doFilter(Map<String, Object> request, String body, boolean isLast) throws IOException {
        if (isLast) {
            return new Object[] {200, null, body.toUpperCase()};
        }else {
            return new Object[] {null, null, body.toUpperCase()};
        }
    }
}