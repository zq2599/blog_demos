package com.bolingcavalry.handlerdemo;

import nginx.clojure.java.ArrayMap;
import nginx.clojure.java.NginxJavaRingHandler;
import javax.xml.bind.DatatypeConverter;
import java.util.Map;
import static nginx.clojure.MiniConstants.DEFAULT_ENCODING;
import static nginx.clojure.MiniConstants.HEADERS;
import static nginx.clojure.java.Constants.PHASE_DONE;

/**
 * @author zq2599@gmail.com
 * @Title: 鉴权功能
 * @Package
 * @Description:
 * @date 2/6/22 9:37 PM
 */
public  class BasicAuthHandler implements NginxJavaRingHandler {

    @Override
    public Object[] invoke(Map<String, Object> request) {
        // 从header中获取authorization字段
        String auth = (String) ((Map)request.get(HEADERS)).get("authorization");
        if (auth == null) {
            return new Object[] { 401, ArrayMap.create("www-authenticate", "Basic realm=\"Secure Area\""),
                    "<HTML><BODY><H1>401 Unauthorized.</H1></BODY></HTML>" };
        }
        String[] up = new String(DatatypeConverter.parseBase64Binary(auth.substring("Basic ".length())), DEFAULT_ENCODING).split(":");
        if (up[0].equals("xfeep") && up[1].equals("hello!")) {
            return PHASE_DONE;
        }
        return new Object[] { 401, ArrayMap.create("www-authenticate", "Basic realm=\"Secure Area\""),
                "<HTML><BODY><H1>401 Unauthorized BAD USER & PASSWORD.</H1></BODY></HTML>" };
    }
}