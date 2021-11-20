package com.bolingcavalry.grabpush;

import static org.bytedeco.ffmpeg.global.avcodec.av_packet_unref;

import java.io.FileInputStream;
import java.io.InputStream;

import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;

import javax.swing.*;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2021/11/19 6:53 上午
 * @description 功能介绍
 */
public class Preview {
    public static void main(String[] args) throws Exception {
//        InputStream in = new FileInputStream("/Users/zhaoqin/temp/202111/19/123.mp4");
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("/Users/zhaoqin/temp/202111/19/123.mp4");
//        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("E:\\temp\\202107\\24\\test.mp4");
        grabber.start();
        CanvasFrame canvasFrame = new CanvasFrame("视频预览");
        canvasFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvasFrame.setAlwaysOnTop(true);
        Frame frame;

        Java2DFrameConverter converter = new Java2DFrameConverter();

        while (null!=(frame=grabber.grabImage())) {
            canvasFrame.showImage(converter.getBufferedImage(frame));
            Thread.sleep(10);
        }

        grabber.close();


    }
}
