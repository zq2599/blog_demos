package com.bolingcavalry.grabpush.controller;

//import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
        String pullUrl = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";
        String pushUrl = "rtmp://192.168.50.43:11935/live/livestream";
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(pullUrl);
        grabber.setOption("rtsp_transport", "tcp");
        grabber.start();


        /*
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

        while (null!=(avPacket= grabber.grabPacket())) {
            recorder.recordPacket(avPacket);
        }

        recorder.close();
        grabber.close();
        */


        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(pushUrl,
                grabber.getImageWidth()*2,
                grabber.getImageHeight()*2,
                grabber.getAudioChannels());

        int v_rs = 25;

        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // avcodec.AV_CODEC_ID_H264
        recorder.setFormat("flv");
        recorder.setFrameRate(v_rs);
        recorder.setGopSize(v_rs);

        recorder.start();

        Frame frame;
        while (null!=(frame=grabber.grab())) {
            recorder.record(frame);
        }

        recorder.close();
        grabber.close();
    }
}
