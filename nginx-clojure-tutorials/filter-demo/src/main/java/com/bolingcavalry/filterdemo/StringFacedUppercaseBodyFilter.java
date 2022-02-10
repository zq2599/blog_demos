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
            // isLast等于true，表示当前web请求过程中最后一次调用doFilter方法，
            // body是完整response body的最后一部分，
            // 此时返回的status应该不为空，这样nginx-clojure框架就会完成body filter的执行流程，将status和聚合后的body返回给客户端
            return new Object[] {200, null, body.toUpperCase()};
        }else {
            // isLast等于false，表示当前web请求过程中，doFilter方法还会被继续调用，当前调用只是多次中的一次而已，
            // body是完整response body的其中一部分，
            // 此时返回的status应该为空，这样nginx-clojure框架就继续body filter的执行流程，继续调用doFilter
            return new Object[] {null, null, body.toUpperCase()};
        }
    }
}