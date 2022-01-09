package com.bolingcavalry.grabpush.extend;

import com.bolingcavalry.grabpush.Util;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.io.File;
import java.net.URL;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * @author willzhao
 * @version 1.0
 * @description 音频相关的服务
 * @date 2021/12/3 8:09
 */
@Slf4j
public class CamShiftDetectService implements DetectService {

    /**
     * 每一帧原始图片的对象
     */
    private Mat grabbedImage = null;

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
    public CamShiftDetectService(String modelFileUrl) {
        this.modelFileUrl = modelFileUrl;
    }


    private Mat mRgba;
    private Mat mGray;


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

    private boolean isDetected = false;

    private Rect mTrackWindow;
    private ObjectTracker objectTracker;
    OpenCVFrameConverter.ToMat converter1 = new OpenCVFrameConverter.ToMat();
    OpenCVFrameConverter.ToOrgOpenCvCoreMat converter2 = new OpenCVFrameConverter.ToOrgOpenCvCoreMat();


    @Override
    public Frame convert(Frame frame) {
        // 由帧转为Mat
        grabbedImage = converter.convert(frame);

        // 灰度Mat
        if (null==mGray) {
            mGray = DetectService.buildGrayImage(grabbedImage);
        }

        // RGBA的Mat
        if (null==mRgba) {
            mRgba = Util.buildRgbaImage(grabbedImage);
        }

        // 当前图片转为灰度图片
        cvtColor(grabbedImage, mGray, CV_BGR2GRAY);

        // 得到opencv的mat，其格式是RGBA
        org.opencv.core.Mat openCVRGBAMat = Util.buildJavacvBGR2OpenCVRGBA(grabbedImage, mRgba);

        // 如果还没有定位过
        if (!isDetected) {
            // 存放检测结果的容器
            RectVector objects = new RectVector();

            // 开始检测
            classifier.detectMultiScale(mGray, objects);

            // 检测结果总数
            long total = objects.size();

            // 如果没有检测到结果，就用原始帧返回
            if (total!=1) {
                objects.close();
                return frame;
            }

            Rect r = objects.get(0);
            int x = r.x(), y = r.y(), w = r.width(), h = r.height();

            objectTracker = new ObjectTracker(openCVRGBAMat);

            // 创建跟踪目标
            objectTracker.createTrackedObject(openCVRGBAMat, new org.opencv.core.Rect(x, y, w, h));

            rectangle(grabbedImage, new Point(x, y), new Point(x + w, y + h), Scalar.RED, 1, CV_AA, 0);

            // 释放检测结果资源
            objects.close();

            isDetected = true;

            // 将标注过的图片转为帧，返回
            return converter.convert(grabbedImage);
        }

        // 基于上一次的检测结果开始跟踪
        org.opencv.core.RotatedRect rotatedRect = objectTracker.objectTracking(openCVRGBAMat);

        if (null!=rotatedRect) {
            org.opencv.core.Rect r = rotatedRect.boundingRect();
            int x = r.x, y = r.y, w = r.width, h = r.height;
            rectangle(grabbedImage, new Point(x, y), new Point(x + w, y + h), Scalar.RED, 1, CV_AA, 0);
            return converter.convert(grabbedImage);
        }

       return frame;
    }

    /**
     * 程序结束前，释放人脸识别的资源
     */
    @Override
    public void releaseOutputResource() {
        if (null!=grabbedImage) {
            grabbedImage.release();
        }

        if (null!=mGray) {
            mGray.release();
        }

        if (null!=mRgba) {
            mRgba.release();
        }

        if (null==classifier) {
            classifier.close();
        }
    }
}
