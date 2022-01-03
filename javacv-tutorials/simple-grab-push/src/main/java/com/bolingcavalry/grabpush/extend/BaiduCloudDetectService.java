package com.bolingcavalry.grabpush.extend;

import com.bolingcavalry.grabpush.bean.response.FaceDetectResponse;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.opencv.face.Face;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_AA;

/**
 * @author willzhao
 * @version 1.0
 * @description 音频相关的服务
 * @date 2021/12/3 8:09
 */
@Slf4j
public class BaiduCloudDetectService implements DetectService {

    /**
     * 每一帧原始图片的对象
     */
    private Mat grabbedImage = null;

    /**
     * 百度云的token
     */
    private String token;

    /**
     * 图片的base64字符串
     */
    private String base64Str;

    /**
     * 百度云服务
     */
    private BaiduCloudService baiduCloudService;

    private OpenCVFrameConverter.ToMat openCVConverter = new OpenCVFrameConverter.ToMat();

    private Java2DFrameConverter java2DConverter = new Java2DFrameConverter();

    private OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

    private BASE64Encoder encoder = new BASE64Encoder();

    /**
     * 构造方法，在此指定模型文件的下载地址
     * @param token
     */
    public BaiduCloudDetectService(String token) {
        this.token = token;
    }

    /**
     * 百度云服务对象的初始化
     * @throws Exception
     */
    @Override
    public void init() throws Exception {
        baiduCloudService = new BaiduCloudService(token);
    }

    @Override
    public Frame convert(Frame frame) {
        // 将原始帧转成base64字符串
        base64Str = frame2Base64(frame);

        // 记录请求开始的时间
        long startTime = System.currentTimeMillis();

        // 交给百度云进行人脸和口罩检测
        FaceDetectResponse faceDetectResponse = baiduCloudService.detect(base64Str);

        // 如果检测失败，就提前返回了
        if (null==faceDetectResponse
         || null==faceDetectResponse.getErrorCode()
         || !"0".equals(faceDetectResponse.getErrorCode())) {
            String desc = "";
            if (null!=faceDetectResponse) {
                desc = String.format("，错误码[%s]，错误信息[%s]", faceDetectResponse.getErrorCode(), faceDetectResponse.getErrorMsg());
            }

            log.error("检测人脸失败", desc);

            // 提前返回
            return frame;
        }

        log.info("检测耗时[{}]ms，结果：{}", (System.currentTimeMillis()-startTime), faceDetectResponse);

        // 如果拿不到检测结果，就返回原始帧
        if (null==faceDetectResponse.getResult()
        || null==faceDetectResponse.getResult().getFaceList()) {
            log.info("未检测到人脸");
            return frame;
        }

        // 取出百度云的检测结果，后面会逐个处理
        List<FaceDetectResponse.Result.Face> list = faceDetectResponse.getResult().getFaceList();
        FaceDetectResponse.Result.Face face;
        FaceDetectResponse.Result.Face.Location location;
        String desc;
        Scalar color;
        int pos_x;
        int pos_y;

        // 如果有检测结果，就根据结果的数据构造矩形框，画在原图上
        for (int i = 0; i < list.size(); i++) {
            face = list.get(i);

            // 每张人脸的位置
            location = face.getLocation();

            int x = (int)location.getLeft();
            int y = (int)location.getHeight();
            int w = (int)location.getWidth();
            int h = (int)location.getHeight();

            // 口罩字段的type等于1表示带口罩，0表示未带口罩
            if (1==face.getMask().getType()) {
                desc = "Mask";
                color = Scalar.GREEN;
            } else {
                desc = "No mask";
                color = Scalar.RED;
            }

            // 在图片上框出人脸
            rectangle(grabbedImage, new Point(x, y), new Point(x + w, y + h), color, 1, CV_AA, 0);

            // 人脸标注的横坐标
            pos_x = Math.max(x-10, 0);
            // 人脸标注的纵坐标
            pos_y = Math.max(y-10, 0);

            // 给人脸做标注，标注是否佩戴口罩
             putText(grabbedImage, desc, new Point(pos_x, pos_y), FONT_HERSHEY_PLAIN, 1.5, color);
        }

        // 将标注过的图片转为帧，返回
        return converter.convert(grabbedImage);
    }

    /**
     * 程序结束前，释放人脸识别的资源
     */
    @Override
    public void releaseOutputResource() {
        if (null!=grabbedImage) {
            grabbedImage.release();
        }
    }

    private String frame2Base64(Frame frame) {
        grabbedImage = converter.convert(frame);
        BufferedImage bufferedImage = java2DConverter.convert(openCVConverter.convert(grabbedImage));
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", bStream);
        } catch (IOException e) {
            throw new RuntimeException("bugImg读取失败:"+e.getMessage(),e);
        }

        return encoder.encode(bStream.toByteArray());
    }
}
