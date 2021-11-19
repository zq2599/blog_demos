package com.bolingcavalry.grabpush.controller;

//import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDateTime;

//@RestController
//@Slf4j
public class GrabPushController {

//    @GetMapping("/grab")
//    public String grab() throws Exception {
////        String url = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";
//        String url = "/Users/zhaoqin/temp/202111/14/100410A000.mp4";
//
//        File tempFile = new File("/Users/zhaoqin/temp/202111/14/", "100410A000.mp4");
//        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(new FileInputStream(tempFile));
//
//
//        return "Success,  " + LocalDateTime.now();
//    }

    public static void main(String[] args) throws Exception {
//        String pullUrl = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";
        String pullUrl = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";
//        String pullUrl = "/Users/zhaoqin/temp/202111/14/100410A000.mp4";
        String pushUrl = "rtmp://192.168.50.43:11935/live/livestream1";


        /*
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(pullUrl);
        grabber.setOption("rtsp_transport", "tcp");
        grabber.start();
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(pushUrl,
                                        grabber.getImageWidth(),
                                        grabber.getImageHeight(),
                                        grabber.getAudioChannels());

        int v_rs = 25;

        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // avcodec.AV_CODEC_ID_H264
        recorder.setFormat("flv");
        recorder.setFrameRate(v_rs);
        recorder.setGopSize(v_rs);

        recorder.start(grabber.getFormatContext());

        AVPacket avPacket;

        long pts, dts;

        while (null!=(avPacket= grabber.grabPacket())) {
            pts = avPacket.pts();
            dts = avPacket.dts();

            if (dts<pts) {
                avPacket.dts(pts);
            }


            recorder.recordPacket(avPacket);
            avcodec.av_packet_unref(avPacket);
        }

        recorder.close();
        grabber.close();
        */

        // rtsp成功推送
        /*
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(pullUrl);
        grabber.setOption("rtsp_transport", "tcp");
        grabber.start();
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(pushUrl,
                grabber.getImageWidth()*2,
                grabber.getImageHeight()*2,
                grabber.getAudioChannels());

        int v_rs = 25;

        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // avcodec.AV_CODEC_ID_H264
        recorder.setFormat("flv");
        recorder.setFrameRate(v_rs);
        recorder.setGopSize(v_rs);
        recorder.setAudioChannels(grabber.getAudioChannels());
        FFmpegLogCallback.set();

        recorder.start();

        Frame frame;
        while (null!=(frame=grabber.grab())) {
            recorder.record(frame);
        }

        recorder.close();
        grabber.close();

         */

        // mp4可以成功预览
        /*
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(pullUrl);
        grabber.setOption("rtsp_transport", "tcp");
        grabber.start();
        CanvasFrame canvasFrame = new CanvasFrame("视频预览");
        canvasFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvasFrame.setAlwaysOnTop(true);
        Frame frame;

        Java2DFrameConverter converter = new Java2DFrameConverter();

        while (null!=(frame=grabber.grabImage())) {
            canvasFrame.showImage(converter.getBufferedImage(frame));
            Thread.sleep(40);
        }

        grabber.close();
        */

        InputStream in = new FileInputStream(pullUrl);
//        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(in, 0);
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(in, 0);
        grabber.setOption("stimeout", "2000000");
        grabber.setVideoOption("vcodec", "copy");
        grabber.setFormat("mp4");
        grabber.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        // h264编/解码器
        grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        grabber.start();



        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(pushUrl,
                grabber.getImageWidth(), grabber.getImageHeight(), 0);
        recorder.setInterleaved(true);
        // 设置比特率
        recorder.setVideoBitrate(2500000);
        // h264编/解码器
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        // 封装flv格式
        recorder.setFormat("flv");
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        // 视频帧率(保证视频质量的情况下最低25，低于25会出现闪屏)
        recorder.setFrameRate(grabber.getFrameRate());
        // 关键帧间隔，一般与帧率相同或者是视频帧率的两倍
        recorder.setGopSize((int) grabber.getFrameRate() * 2);
        AVFormatContext fc = null;
        fc = grabber.getFormatContext();

        recorder.start(fc);
        // 清空探测时留下的缓存
        grabber.flush();

        /*
        CanvasFrame canvasFrame = new CanvasFrame("视频预览");
        canvasFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvasFrame.setAlwaysOnTop(true);
        Frame frame;

        Java2DFrameConverter converter = new Java2DFrameConverter();

        while (null!=(frame=grabber.grabImage())) {
            canvasFrame.showImage(converter.getBufferedImage(frame));
            Thread.sleep(40);
        }
        */

        grabber.close();



    }
}
