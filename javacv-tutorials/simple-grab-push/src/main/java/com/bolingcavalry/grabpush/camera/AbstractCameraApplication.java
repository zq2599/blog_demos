package com.bolingcavalry.grabpush.camera;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.bytedeco.opencv.global.opencv_core.cvFlip;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2021/11/19 8:07 上午
 * @description 摄像头应用的基础类，这里面定义了拉流和推流的基本流程，子类只需实现具体的业务方法即可
 */
@Slf4j
public abstract class AbstractCameraApplication {

    /**
     * 摄像头序号，如果只有一个摄像头，那就是0
     */
    protected static final int CAMERA_INDEX = 0;

    /**
     * 帧抓取器
     */
    protected FrameGrabber grabber;

    /**
     * 输出帧率
     */
    @Getter
    private final double frameRate = 30;

    /**
     * 摄像头视频的宽
     */
    @Getter
    private final int cameraImageWidth = 1280;

    /**
     * 摄像头视频的高
     */
    @Getter
    private final int cameraImageHeight = 720;

    /**
     * 转换器
     */
    private final OpenCVFrameConverter.ToIplImage openCVConverter = new OpenCVFrameConverter.ToIplImage();

    /**
     * 实例化、初始化输出操作相关的资源
     */
    protected abstract void initOutput() throws Exception;

    /**
     * 输出
     */
    protected abstract void output(Frame frame) throws Exception;

    /**
     * 释放输出操作相关的资源
     */
    protected abstract void releaseOutputResource() throws Exception;

    /**
     * 两帧之间的间隔时间
     * @return
     */
    protected int getInterval() {
        // 假设一秒钟15帧，那么两帧间隔就是(1000/15)毫秒
        return (int)(1000/ frameRate);
    }

    /**
     * 实例化帧抓取器，默认OpenCVFrameGrabber对象，
     * 子类可按需要自行覆盖
     * @throws FFmpegFrameGrabber.Exception
     */
    protected void instanceGrabber() throws FrameGrabber.Exception {
        grabber = new OpenCVFrameGrabber(CAMERA_INDEX);
    }

    /**
     * 用帧抓取器抓取一帧，默认调用grab()方法，
     * 子类可以按需求自行覆盖
     * @return
     */
    protected Frame grabFrame() throws FrameGrabber.Exception {
        return grabber.grab();
    }

    /**
     * 初始化帧抓取器
     * @throws Exception
     */
    protected void initGrabber() throws Exception {
        // 实例化帧抓取器
        instanceGrabber();

        // 摄像头有可能有多个分辨率，这里指定
        // 可以指定宽高，也可以不指定反而调用grabber.getImageWidth去获取，
        grabber.setImageWidth(cameraImageWidth);
        grabber.setImageHeight(cameraImageHeight);

        // 开启抓取器
        grabber.start();
    }

    /**
     * 预览和输出
     * @param grabSeconds 持续时长
     * @throws Exception
     */
    private void grabAndOutput(int grabSeconds) throws Exception {
        // 添加水印时用到的时间工具
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        long endTime = System.currentTimeMillis() + 1000L *grabSeconds;

        // 两帧输出之间的间隔时间，默认是1000除以帧率，子类可酌情修改
        int interVal = getInterval();

        // 水印在图片上的位置
        org.bytedeco.opencv.opencv_core.Point point = new org.bytedeco.opencv.opencv_core.Point(15, 35);

        Frame captureFrame;
        IplImage img;
        Mat mat;

        // 超过指定时间就结束循环
        while (System.currentTimeMillis()<endTime) {
            // 取一帧
            captureFrame = grabFrame();

            if (null==captureFrame) {
                log.error("帧对象为空");
                break;
            }

            // 将帧对象转为IplImage对象
            img = openCVConverter.convert(captureFrame);

            // 镜像翻转
            cvFlip(img, img, 1);

            // IplImage转mat
            mat = new Mat(img);

            // 在图片上添加水印，水印内容是当前时间，位置是左上角
            opencv_imgproc.putText(mat,
                    simpleDateFormat.format(new Date()),
                    point,
                    opencv_imgproc.CV_FONT_VECTOR0,
                    0.8,
                    new Scalar(0, 200, 255, 0),
                    1,
                    0,
                    false);

            // 子类输出
            output(openCVConverter.convert(mat));

            // 适当间隔，让肉感感受不到闪屏即可
            if(interVal>0) {
                Thread.sleep(interVal);
            }
        }

        log.info("输出结束");
    }

    /**
     * 释放所有资源
     */
    private void safeRelease() {
        try {
            // 子类需要释放的资源
            releaseOutputResource();
        } catch (Exception exception) {
            log.error("do releaseOutputResource error", exception);
        }

        if (null!=grabber) {
            try {
                grabber.close();
            } catch (Exception exception) {
                log.error("close grabber error", exception);
            }
        }
    }

    /**
     * 整合了所有初始化操作
     * @throws Exception
     */
    private void init() throws Exception {
        long startTime = System.currentTimeMillis();

        // 设置ffmepg日志级别
        avutil.av_log_set_level(avutil.AV_LOG_INFO);
        FFmpegLogCallback.set();

        // 实例化、初始化帧抓取器
        initGrabber();

        // 实例化、初始化输出操作相关的资源，
        // 具体怎么输出由子类决定，例如窗口预览、存视频文件等
        initOutput();

        log.info("初始化完成，耗时[{}]毫秒，帧率[{}]，图像宽度[{}]，图像高度[{}]",
                System.currentTimeMillis()-startTime,
                frameRate,
                cameraImageWidth,
                cameraImageHeight);
    }

    /**
     * 执行抓取和输出的操作
     */
    public void action(int grabSeconds) {
        try {
            // 初始化操作
            init();
            // 持续拉取和推送
            grabAndOutput(grabSeconds);
        } catch (Exception exception) {
            log.error("execute action error", exception);
        } finally {
            // 无论如何都要释放资源
            safeRelease();
        }
    }
}
