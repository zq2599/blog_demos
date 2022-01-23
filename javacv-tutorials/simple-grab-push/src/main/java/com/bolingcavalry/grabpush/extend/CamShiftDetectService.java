package com.bolingcavalry.grabpush.extend;

import com.bolingcavalry.grabpush.Util;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import java.io.File;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;

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
    private String modelFilePath;

    /**
     * 存放RGBA图片Mat
     */
    private Mat mRgba;

    /**
     * 存放灰度图片的Mat，仅用在人脸检测的时候
     */
    private Mat mGray;

    /**
     * 跟踪服务类
     */
    private ObjectTracker objectTracker;

    /**
     * 表示当前是否正在跟踪目标
     */
    private boolean isInTracing = false;

    /**
     * 构造方法，在此指定模型文件的下载地址
     * @param modelFilePath
     */
    public CamShiftDetectService(String modelFilePath) {
        this.modelFilePath = modelFilePath;
    }

    /**
     * 音频采样对象的初始化
     * @throws Exception
     */
    @Override
    public void init() throws Exception {
        log.info("开始加载模型文件");
        // 模型文件下载后的完整地址
        String classifierName = new File(modelFilePath).getAbsolutePath();

        // 根据模型文件实例化分类器
        classifier = new CascadeClassifier(classifierName);

        if (classifier == null) {
            log.error("Error loading classifier file [{}]", classifierName);
            System.exit(1);
        }

        log.info("模型文件加载完毕，初始化完成");
    }



    @Override
    public Frame convert(Frame frame) {
        // 由帧转为Mat
        grabbedImage = converter.convert(frame);

        // 初始化灰度Mat
        if (null==mGray) {
            mGray = Util.initGrayImageMat(grabbedImage);
        }

        // 初始化RGBA的Mat
        if (null==mRgba) {
            mRgba = Util.initRgbaImageMat(grabbedImage);
        }

        // 如果未在追踪状态
        if (!isInTracing) {
            // 存放检测结果的容器
            RectVector objects = new RectVector();

            // 当前图片转为灰度图片
            cvtColor(grabbedImage, mGray, CV_BGR2GRAY);

            // 开始检测
            classifier.detectMultiScale(mGray, objects);

            // 检测结果总数
            long total = objects.size();

            // 当前实例是只追踪一人，因此一旦检测结果不等于一，就不处理，您可以根据自己业务情况修改此处
            if (total!=1) {
                objects.close();
                return frame;
            }

            log.info("start new trace");

            Rect r = objects.get(0);
            int x = r.x(), y = r.y(), w = r.width(), h = r.height();

            // 得到opencv的mat，其格式是RGBA
            org.opencv.core.Mat openCVRGBAMat = Util.buildJavacvBGR2OpenCVRGBA(grabbedImage, mRgba);

            // 在buildJavacvBGR2OpenCVRGBA方法内部，有可能在执行native方法的是否发生异常，要做针对性处理
            if (null==openCVRGBAMat) {
                objects.close();
                return frame;
            }

            // 如果第一次追踪，要实例化objectTracker
            if (null==objectTracker) {
                objectTracker = new ObjectTracker(openCVRGBAMat);
            }

            // 创建跟踪目标
            objectTracker.createTrackedObject(openCVRGBAMat, new org.opencv.core.Rect(x, y, w, h));
            // 根据本次检测结果给原图标注人脸矩形框
            Util.rectOnImage(grabbedImage, x, y, w, h);

            // 释放检测结果资源
            objects.close();

            // 修改标志，表示当前正在跟踪
            isInTracing = true;

            // 将标注过的图片转为帧，返回
            return converter.convert(grabbedImage);
        }

        // 代码走到这里，表示已经在追踪状态了

        // 得到opencv的mat，其格式是RGBA
        org.opencv.core.Mat openCVRGBAMat = Util.buildJavacvBGR2OpenCVRGBA(grabbedImage, mRgba);

        // 在buildJavacvBGR2OpenCVRGBA方法内部，有可能在执行native方法的是否发生异常，要做针对性处理
        if (null==openCVRGBAMat) {
            return frame;
        }

        // 基于上一次的检测结果开始跟踪
        org.opencv.core.Rect rotatedRect = objectTracker.objectTracking(openCVRGBAMat);

        // 如果rotatedRect为空，表示跟踪失败，此时要修改状态为"未跟踪"
        if (null==rotatedRect) {
            isInTracing = false;
            // 返回原始帧
            return frame;
        }

        // 代码能走到这里，表示跟踪成功，拿到的新的一帧上的目标的位置，此时就在新位置上
//        Util.rectOnImage(grabbedImage, rotatedRect.x, rotatedRect.y, rotatedRect.width, rotatedRect.height);
        // 矩形框的整体向下放一些(总高度的五分之一)，另外跟踪得到的高度过大，画出的矩形框把脖子也框上了，这里改用宽度作为高度
        Util.rectOnImage(grabbedImage, rotatedRect.x, rotatedRect.y + rotatedRect.height/5, rotatedRect.width, rotatedRect.width);
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
