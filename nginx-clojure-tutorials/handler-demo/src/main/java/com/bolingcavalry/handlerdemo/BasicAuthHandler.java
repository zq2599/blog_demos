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

        // 如果header中没有authorization，就返回401错误，并带上body
        if (auth == null) {
            return new Object[] { 401, ArrayMap.create("www-authenticate", "Basic realm=\"Secure Area\""),
                    "<HTML><BODY><H1>401 Unauthorized.</H1></BODY></HTML>" };
        }

        // authorization应该是 : Basic xfeep:hello!，所以这里先将"Basic "去掉，然后再用":"分割
        String[] up = auth.substring("Basic ".length()).split(":");

        // 只是为了演示，所以账号和密码的检查逻辑在代码中是写死的，
        // 如果账号等于"xfeep"，并且密码等于"hello!"，就返回PHASE_DONE，这样nginx-clojure就会继续执行后面的content handler
        if (up[0].equals("xfeep") && up[1].equals("hello!")) {
            return PHASE_DONE;
        }

        // 如果账号密码校验不过，就返回401，body内容是提示账号密码不过
        return new Object[] { 401, ArrayMap.create("www-authenticate", "Basic realm=\"Secure Area\""),
                "<HTML><BODY><H1>401 Unauthorized BAD USER & PASSWORD.</H1></BODY></HTML>" };
    }
}