package com.bolingcavalry;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class App {
    public static void main( String[] args ) throws Exception {
        for(int i=0;i<10;i++) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ref", "ref-"+i);
            jsonObject.put("repositoryURL","https://github.com/zq2599/jenkinsdemo.git");
            jsonObject.put("branch", "master");

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("http://192.168.133.149:32049/generic-webhook-trigger/invoke?token=token-remote-test");
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(jsonObject.toJSONString()));
            CloseableHttpResponse response = httpClient.execute(httpPost);
            response.close();
            httpClient.close();

            System.out.println("response code : " + response.getStatusLine().getStatusCode() + "\n");
        }
    }
}
