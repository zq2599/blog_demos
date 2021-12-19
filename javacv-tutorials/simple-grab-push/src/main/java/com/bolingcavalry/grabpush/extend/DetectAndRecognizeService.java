package com.bolingcavalry.grabpush.extend;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.io.File;
import java.net.URL;
import java.util.Map;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * @author willzhao
 * @version 1.0
 * @description 音频相关的服务
 * @date 2021/12/3 8:09
 */
@Slf4j
public class DetectAndRecognizeService implements DetectService {

    /**
     * 每一帧原始图片的对象
     */
    private Mat grabbedImage = null;

    /**
     * 原始图片对应的灰度图片对象
     */
    private Mat grayImage = null;

    /**
     * 分类器
     */
    private CascadeClassifier classifier;

    /**
     * 转换器
     */
    private OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

    /**
     * 检测模型文件的下载地址
     */
    private String detectModelFileUrl;

    /**
     * 处理每一帧的服务
     */
    private RecognizeService recognizeService;

    /**
     * 为了显示的时候更加友好，给每个分类对应一个名称
     */
    private Map<Integer, String> kindNameMap;

    /**
     * 构造方法
     * @param detectModelFileUrl
     * @param recognizeModelFilePath
     * @param kindNameMap
     */
    public DetectAndRecognizeService(String detectModelFileUrl, String recognizeModelFilePath, Map<Integer, String> kindNameMap) {
        this.detectModelFileUrl = detectModelFileUrl;
        this.recognizeService = new RecognizeService(recognizeModelFilePath);
        this.kindNameMap = kindNameMap;
    }

    /**
     * 音频采样对象的初始化
     * @throws Exception
     */
    @Override
    public void init() throws Exception {
        // 下载模型文件
        URL url = new URL(detectModelFileUrl);

        File file = Loader.cacheResource(url);

        // 模型文件下载后的完整地址
        String classifierName = file.getAbsolutePath();

        // 根据模型文件实例化分类器
        classifier = new CascadeClassifier(classifierName);

        if (classifier == null) {
            log.error("Error loading classifier file [{}]", classifierName);
            System.exit(1);
        }
    }

    @Override
    public Frame convert(Frame frame) {
        // 由帧转为Mat
        grabbedImage = converter.convert(frame);

        // 灰度Mat，用于检测
        if (null==grayImage) {
            grayImage = DetectService.buildGrayImage(grabbedImage);
        }

        // 进行人脸识别，根据结果做处理得到预览窗口显示的帧
        return detectAndRecoginze(classifier, converter, frame, grabbedImage, grayImage, recognizeService, kindNameMap);
    }

    /**
     * 程序结束前，释放人脸识别的资源
     */
    @Override
    public void releaseOutputResource() {
        if (null!=grabbedImage) {
            grabbedImage.release();
        }

        if (null!=grayImage) {
            grayImage.release();
        }

        if (null==classifier) {
            classifier.close();
        }
    }

    /**
     * 检测图片，将检测结果用矩形标注在原始图片上
     * @param classifier 分类器
     * @param converter Frame和mat的转换器
     * @param rawFrame 原始视频帧
     * @param grabbedImage 原始视频帧对应的mat
     * @param grayImage 存放灰度图片的mat
     * @param kindNameMap 每个分类编号对应的名称
     * @return 标注了识别结果的视频帧
     */
    static Frame detectAndRecoginze(CascadeClassifier classifier,
                                    OpenCVFrameConverter.ToMat converter,
                                    Frame rawFrame,
                                    Mat grabbedImage,
                                    Mat grayImage,
                                    RecognizeService recognizeService,
                                    Map<Integer, String> kindNameMap) {

        // 当前图片转为灰度图片
        cvtColor(grabbedImage, grayImage, CV_BGR2GRAY);

        // 存放检测结果的容器
        RectVector objects = new RectVector();

        // 开始检测
        classifier.detectMultiScale(grayImage, objects);

        // 检测结果总数
        long total = objects.size();

        // 如果没有检测到结果，就用原始帧返回
        if (total<1) {
            return rawFrame;
        }

        PredictRlt predictRlt;
        int pos_x;
        int pos_y;
        int lable;
        double confidence;
        String content;

        // 如果有检测结果，就根据结果的数据构造矩形框，画在原图上
        for (long i = 0; i < total; i++) {
            Rect r = objects.get(i);

            predictRlt = recognizeService.predict(new Mat(grayImage, r));

            // 如果返回为空，表示出现过异常，就执行下一个
            if (null==predictRlt) {
                System.out.println("return null");
                continue;
            }

            // 分类的编号（训练时只有1和2，这里只有有三个值，1和2与训练的分类一致，还有个-1表示没有匹配上）
            lable = predictRlt.getLable();
            // 与模型中的分类的距离，值越小表示相似度越高
            confidence = predictRlt.getConfidence();

            // 得到分类编号后，从map中取得名字，用来显示
            if (kindNameMap.containsKey(predictRlt.getLable())) {
                content = String.format("%s, confidence : %.4f", kindNameMap.get(lable), confidence);
            } else {
                // 取不到名字的时候，就显示unknown
                content = "unknown(" + predictRlt.getLable() + ")";
                System.out.println(content);
            }

            int x = r.x(), y = r.y(), w = r.width(), h = r.height();
            rectangle(grabbedImage, new Point(x, y), new Point(x + w, y + h), Scalar.RED, 1, CV_AA, 0);

            pos_x = Math.max(r.tl().x()-10, 0);
            pos_y = Math.max(r.tl().y()-10, 0);

            putText(grabbedImage, content, new Point(pos_x, pos_y), FONT_HERSHEY_PLAIN, 1.5, new Scalar(0,255,0,2.0));
        }

        // 释放检测结果资源
        objects.close();

        // 将标注过的图片转为帧，返回
        return converter.convert(grabbedImage);
    }
}
