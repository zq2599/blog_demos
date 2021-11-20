package com.bolingcavalry.grabpush;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2021/11/19 8:49
 */
public class Mp4Pusher {
    public static void main(String[] args) throws Exception {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("/Users/zhaoqin/temp/202111/19/123.mp4");
        String pushUrl = "rtmp://192.168.50.43:11935/live/livestream1";
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
    }
}
