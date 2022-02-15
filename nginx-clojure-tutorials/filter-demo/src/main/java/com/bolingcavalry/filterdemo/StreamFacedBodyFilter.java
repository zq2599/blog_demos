package com.bolingcavalry.filterdemo;

import nginx.clojure.NginxChainWrappedInputStream;
import nginx.clojure.NginxClojureRT;
import nginx.clojure.java.NginxJavaBodyFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2022/2/14 9:47 下午
 * @description 功能介绍
 */
public class StreamFacedBodyFilter implements NginxJavaBodyFilter {

    @Override
    public Object[] doFilter(Map<String, Object> request, InputStream bodyChunk, boolean isLast) throws IOException {
        // 这里仅将二进制文件长度打印到日志，您可以按照业务实际情况自行修改
        NginxClojureRT.log.info("isLast [%s], total [%s]", String.valueOf(isLast), String.valueOf(bodyChunk.available()));

        // NginxChainWrappedInputStream的成员变量index记录的读取的位置，本次用完后要重置位置，因为doFilter之外的代码中可能也会读取bodyChunk
        ((NginxChainWrappedInputStream)bodyChunk).rewind();

        if (isLast) {
            // isLast等于true，表示当前web请求过程中最后一次调用doFilter方法，
            // body是完整response body的最后一部分，
            // 此时返回的status应该不为空，这样nginx-clojure框架就会完成body filter的执行流程，将status和聚合后的body返回给客户端
            return new Object[] {200, null, bodyChunk};
        }else {
            // isLast等于false，表示当前web请求过程中，doFilter方法还会被继续调用，当前调用只是多次中的一次而已，
            // body是完整response body的其中一部分，
            // 此时返回的status应该为空，这样nginx-clojure框架就继续body filter的执行流程，继续调用doFilter
            return new Object[] {null, null, bodyChunk};
        }
    }
}
