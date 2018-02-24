package com.bolingcavalry;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;

/**
 * @Description : 上传文件的类，将本地文件POST到server
 * @Author : zq2599@gmail.com
 * @Date : 2018-02-24 18:12
 */
public class UploadFileClient {

    /**
     * 文件服务的ULR
     */
    private static final String POST_URL = "http://192.168.119.155:8080/upload";

    /**
     * 要上传的本地文件的完整路径加文件名
     */
    private static final String UPLOAD_FILE_FULLPATH = "D:\\temp\\201802\\21\\abc.zip";

    public static void main(String[] args) throws Exception{
        System.out.println("start upload");
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httppost = new HttpPost(POST_URL);

            //基本的配置信息
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(200000).setSocketTimeout(200000).build();

            httppost.setConfig(requestConfig);

            //要上传的文件
            FileBody bin = new FileBody(new File(UPLOAD_FILE_FULLPATH));

            //在POST中添加一个字符串请求参数
            StringBody comment = new StringBody("This is comment", ContentType.TEXT_PLAIN);

            HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("file", bin).addPart("comment", comment).build();

            httppost.setEntity(reqEntity);

            System.out.println("executing request " + httppost.getRequestLine());

            //发起POST
            CloseableHttpResponse response = httpclient.execute(httppost);

            try {

                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    String responseEntityStr = EntityUtils.toString(response.getEntity());
                    System.out.println("response status : " + response.getStatusLine());
                    System.out.println("response content length: " + resEntity.getContentLength());
                    System.out.println("response entity str : " + responseEntityStr);
                }
                EntityUtils.consume(resEntity);
            } finally {
                response.close();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("end upload");
    }
}
