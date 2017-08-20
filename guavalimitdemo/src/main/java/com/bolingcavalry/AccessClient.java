package com.bolingcavalry;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Description:模拟多线程请求
 * @author willzhao
 * @email zq2599@gmail.com
 * @date 2017/7/9 12:11
 */
public class AccessClient {
    ExecutorService fixedThreadPool = Executors.newFixedThreadPool(10);

    /**
     * get请求
     * @param realUrl
     * @return
     */
    public static String sendGet(URL realUrl) {
        String result = "";
        BufferedReader in = null;
        try {
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();

            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }



    public void access() throws Exception{
        final URL url = new URL("http://localhost:8080/guavalimitdemo/access");

        for(int i=0;i<10;i++) {
            fixedThreadPool.submit(new Runnable() {
                public void run() {
                    System.out.println(sendGet(url));
                }
            });
        }

        fixedThreadPool.shutdown();
        fixedThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws Exception{
        AccessClient accessClient = new AccessClient();
        accessClient.access();
    }
}
