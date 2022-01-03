package com.bolingcavalry.grabpush.extend;

import com.bolingcavalry.grabpush.bean.request.FaceDetectRequest;
import com.bolingcavalry.grabpush.bean.response.FaceDetectResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import sun.misc.BASE64Encoder;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author willzhao
 * @version 1.0
 * @description 百度云服务的调用
 * @date 2022/1/1 11:06
 */
public class BaiduCloudService {

    // 转换
    BASE64Encoder encoder = new BASE64Encoder();

    OkHttpClient client = new OkHttpClient();

    static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    static final String URL_TEMPLATE = "https://aip.baidubce.com/rest/2.0/face/v3/detect?access_token=%s";

    String token;

    ObjectMapper mapper = new ObjectMapper();

    public BaiduCloudService(String token) {
        this.token = token;

        // 重要：反序列化的时候，字符的字段如果比类的字段多，下面这个设置可以确保反序列化成功
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * 将指定位置的图片转为base64字符串
     * @param imagePath
     * @return
     */
    private String img2Base64(String imagePath) {
        InputStream inputStream = null;
        byte[] data = null;

        try {
            inputStream = new FileInputStream(imagePath);
            data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return null==data ? null :encoder.encode(data);
    }

    /**
     * 检测指定的图片
     * @param imageBase64
     * @return
     */
    public FaceDetectResponse detect(String imageBase64) {
        // 请求对象
        FaceDetectRequest faceDetectRequest = new FaceDetectRequest();
        faceDetectRequest.setImageType("BASE64");
        faceDetectRequest.setFaceField("mask");
        faceDetectRequest.setMaxFaceNum(6);
        faceDetectRequest.setFaceType("LIVE");
        faceDetectRequest.setLivenessControl("NONE");
        faceDetectRequest.setFaceSortType(0);
        faceDetectRequest.setImage(imageBase64);

        FaceDetectResponse faceDetectResponse = null;

        try {
            // 用Jackson将请求对象序列化成字符串
            String jsonContent = mapper.writeValueAsString(faceDetectRequest);

            //
            RequestBody requestBody = RequestBody.create(JSON, jsonContent);
            Request request = new Request
                    .Builder()
                    .url(String.format(URL_TEMPLATE, token))
                    .post(requestBody)
                    .build();
            Response response = client.newCall(request).execute();
            String rawRlt = response.body().string();
            faceDetectResponse = mapper.readValue(rawRlt, FaceDetectResponse.class);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return faceDetectResponse;
    }

    public static void main(String[] args) {
        String imagePath = "E:\\temp\\202201\\01\\pic\\1.jpeg";
        // 图片在本地的位置
//        String imagePath = "E:\\temp\\202201\\01\\pic\\2.png";

        // 百度云的token，是通过此接口得到的：https://aip.baidubce.com/oauth/2.0/token
        String token = "24.cea0b26a4191b380fe4259c4777e9e40.2592000.1643799009.282335-25415527";

        // 实例化服务对象
        BaiduCloudService service = new BaiduCloudService(token);

        // 将图片转为base64字符串
        String imageBase64 = service.img2Base64(imagePath);

        // 向百度服务发请求，检测人脸
        FaceDetectResponse faceDetectResponse = service.detect(imageBase64);

        // 输出检测结果
        System.out.println(faceDetectResponse);
    }

}
