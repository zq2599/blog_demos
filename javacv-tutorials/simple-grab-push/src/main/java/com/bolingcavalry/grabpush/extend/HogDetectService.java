package com.bolingcavalry.grabpush.extend;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.io.File;
import java.net.URL;

/**
 * @author willzhao
 * @version 1.0
 * @description 音频相关的服务
 * @date 2021/12/3 8:09
 */
@Slf4j
public class HogDetectService implements DetectService {

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

    public HogDetectService(String modelPath) {
        this.modelPath = modelPath;
    }

    private String modelPath;

    /**
     * 音频采样对象的初始化
     * @throws Exception
     */
    @Override
    public void init() throws Exception {

        URL url = new URL(modelPath);
        File file = Loader.cacheResource(url);
        String classifierName = file.getAbsolutePath();

        // We can "cast" Pointer objects by instantiating a new object of the desired class.
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
        return DetectService.detect(classifier, converter, frame, grabbedImage, grayImage);
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
}
