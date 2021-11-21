package com.bolingcavalry.grabpush;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.bytedeco.opencv.presets.opencv_objdetect;

import javax.swing.*;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2021/11/19 8:07 上午
 * @description 功能介绍
 */
@Slf4j
public class PushCamera {

    /**
     * 保存MP4文件的完整路径(两分零五秒的视频)
     */
    private static final String MP4_FILE_PATH = "/Users/zhaoqin/temp/202111/21/camera.mp4";

    /**
     * SRS的推流地址
     */
    private static final String SRS_PUSH_ADDRESS = "rtmp://192.168.50.43:11935/live/livestream";

    /**
     * 摄像头序号，如果只有一个摄像头，那就是0
     */
    private static final int CAMERA_INDEX = 0;

    /**
     * 本机窗口
     */
    private CanvasFrame previewCanvas;

    /**
     * 帧抓取器
     */
    private FrameGrabber grabber;

    /**
     * 帧录制器
     */
    private FrameRecorder recorder;

    /**
     * 转换类
     */
    private OpenCVFrameConverter.ToIplImage converter;

    /**
     * 输出帧率
     */
    private static final double FRAME_RATE = 30.0;

    /**
     * 摄像头视频的宽
     */
    private int cameraImageWidth;

    /**
     * 摄像头视频的高
     */
    private int cameraImageHeight;

    /**
     * 每一次从摄像头抓取的帧都暂存在这里
     */
    private IplImage grabbedImage;

    /**
     * 初始化帧抓取器
     * @throws Exception
     */
    private void initGrabber() throws Exception {
        // 本机摄像头默认0，这里使用javacv的抓取器，至于使用的是ffmpeg还是opencv，请自行查看源码
        grabber = FrameGrabber.createDefault(CAMERA_INDEX);

        // 开启抓取器
        grabber.start();
    }

    /**
     * 初始化帧录制器
     * @throws Exception
     */
    private void initRecorder() throws Exception {
        // 实例化帧录制器
        recorder = FrameRecorder.createDefault(SRS_PUSH_ADDRESS, cameraImageWidth, cameraImageHeight);

        // 设置编码器
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);

        // 封装格式
        recorder.setFormat("flv");

        // 设置帧录制器的帧率
        recorder.setFrameRate(FRAME_RATE);

        // 初始化帧录制器
        recorder.start();
    }

    /**
     * 初始化转换器
     * @throws Exception
     */
    private void initConverter() throws Exception {
        // 实例化转换器
        converter = new OpenCVFrameConverter.ToIplImage();

        // 抓取一帧视频并将其转换为图像，至于用这个图像用来做什么？加水印，人脸识别等等自行添加
        grabbedImage = converter.convert(grabber.grab());

        // 将视频图像的宽度存储在成员变量cameraImageWidth
        cameraImageWidth = grabbedImage.width();

        // 将视频图像的高度存储在成员变量cameraImageHeight
        cameraImageHeight = grabbedImage.height();
    }

    /**
     * 实例化、初始化窗口
     */
    private void initWindow() {
        previewCanvas = new CanvasFrame("摄像头预览", CanvasFrame.getDefaultGamma() / grabber.getGamma());
        previewCanvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        previewCanvas.setAlwaysOnTop(true);
    }

    /**
     * 预览和推送
     * @throws Exception
     */
    private void grabAndPush() throws Exception {
        // 假设一秒钟15帧，那么两帧间隔就是(1000/15)毫秒
        double interVal = 1000/ FRAME_RATE;
        // 发送完一帧后sleep的时间，不能完全等于(1000/frameRate)，不然会卡顿，
        // 要更小一些，这里取八分之一
        interVal/=8.0;

        // 不知道为什么这里不做转换就不能推到rtmp
        Frame rotatedFrame;
        long startTime = System.currentTimeMillis();

        while ((grabbedImage = converter.convert(grabber.grab())) != null) {
            rotatedFrame = converter.convert(grabbedImage);

            // 预览窗口上显示当前帧
//            previewCanvas.showImage(rotatedFrame);

            // 推送的时候，给当前帧加上时间戳
            recorder.setTimestamp(1000 * (System.currentTimeMillis() - startTime));

            // 推送到SRS
            recorder.record(rotatedFrame);

            Thread.sleep((int)interVal);
        }
    }

    /**
     * 释放所有资源
     * @throws Exception
     */
    private void safeRelease() {
        if (null!= previewCanvas) {
            previewCanvas.dispose();
        }

        if (null!=recorder) {
            try {
                recorder.close();
            } catch (Exception exception) {
                log.error("close recorder error", exception);
            }
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

        // 加载检测
        Loader.load(opencv_objdetect.class);

        // 实例化、初始化帧抓取器
        initGrabber();

        // 实例化、初始化转换工具，里面会取得摄像头图像的宽度和高度
        initConverter();

        // 实例化、初始化帧录制器
        initRecorder();

        // 实例化、初始化窗口
//        initWindow();

        log.info("初始化完成，耗时[{}]毫秒，图像宽度[{}]，图像高度[{}]",
                System.currentTimeMillis()-startTime,
                cameraImageWidth,
                cameraImageWidth);
    }

    /**
     * 直播
     */
    public void live() {
        try {
            // 初始化操作
            init();
            // 持续拉取和推送
            grabAndPush();
        } catch (Exception exception) {
            log.error("execute live error", exception);
        } finally {
            // 无论如何都要释放资源
            safeRelease();
        }
    }

    public static void main(String[] args) {
        new PushCamera().live();
    }
}
