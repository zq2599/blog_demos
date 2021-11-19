package com.bolingcavalry.grabpush;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2021/11/19 8:04 上午
 * @description 功能介绍
 */
public class PushMov {
    public static void main(String[] args) throws Exception {
        String pullUrl = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";
        String pushUrl = "rtmp://192.168.50.43:11935/live/livestream1";

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
    }
}
