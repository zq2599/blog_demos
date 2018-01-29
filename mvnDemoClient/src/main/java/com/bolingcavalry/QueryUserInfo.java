package com.bolingcavalry;

import com.alibaba.fastjson.JSONObject;
import com.bolingcavalry.bean.UserInfo;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 * @Description : 发起http请求，将响应的字符串转成对象
 * @Author : zq2599@gmail.com
 * @Date : 2018-01-19 15:56
 */
public class QueryUserInfo {
    public static void main(String[] args) throws Exception{
        CloseableHttpClient httpclient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet("http://localhost:8080/getuserinfo/jerry");
        //response
        HttpResponse response = httpclient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        //收到原始的响应
        String rawStr = EntityUtils.toString(entity,"UTF-8");
        //打印原始的字符串
        System.out.println("raw string : " + rawStr);
        //将字符串转成UserInfo对象
        UserInfo userInfo = JSONObject.parseObject(rawStr, UserInfo.class);
        System.out.println("userName : " + userInfo.getName() + ", userAge : " + userInfo.getAge());
    }
}
