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
public class PushMp4 {
    public static void main(String[] args) throws Exception {
        String pushUrl = "rtmp://192.168.50.43:11935/live/livestream2";
        InputStream in = new FileInputStream("E:\\temp\\202107\\24\\test.mp4");
        // 不能用maximumSize=0，否则会立即退出
        //FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(in, 0);
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(in);
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
