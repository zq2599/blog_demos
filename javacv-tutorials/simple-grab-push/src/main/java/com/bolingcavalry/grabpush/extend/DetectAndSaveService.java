package com.bolingcavalry.grabpush.extend;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author willzhao
 * @version 1.0
 * @description 音频相关的服务
 * @date 2021/12/3 8:09
 */
@Slf4j
public class DetectAndSaveService implements DetectService {

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
     * 模型文件的下载地址
     */
    private String modelFileUrl;

    /**
     * 构造方法，在此指定模型文件的下载地址
     * @param modelFileUrl
     */
    public DetectAndSaveService(String modelFileUrl) {
        this.modelFileUrl = modelFileUrl;
    }

    /**
     * 音频采样对象的初始化
     * @throws Exception
     */
    @Override
    public void init() throws Exception {
        // 下载模型文件
        URL url = new URL(modelFileUrl);
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

        String basePath = "E:\\temp\\202112\\15\\003\\me\\"
                        + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                        + "-";

        // 进行人脸识别，根据结果做处理得到预览窗口显示的帧
        return DetectService.detectAndSave(classifier, converter, frame, grabbedImage, grayImage, basePath, 1);
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
