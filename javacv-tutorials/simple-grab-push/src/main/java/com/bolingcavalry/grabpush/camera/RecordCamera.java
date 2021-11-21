package com.bolingcavalry.grabpush.camera;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;

@Slf4j
public class RecordCamera extends AbstractCameraApplication {

//    private static final String RECORD_ADDRESS = "/Users/zhaoqin/temp/202111/20/camera.mp4";
    private static final String RECORD_ADDRESS = "rtmp://192.168.50.43:11935/live/livestream";

    protected FrameRecorder recorder;

    protected long startRecordTime = 0L;

    public RecordCamera(double frameRate) {
        super(frameRate);
    }

    @Override
    protected void initOutput() throws Exception {
        // 实例化FFmpegFrameRecorder，将SRS的推送地址传入
        recorder = FrameRecorder.createDefault(RECORD_ADDRESS, getCameraImageWidth(), getCameraImageHeight());

        // 设置封装格式
        recorder.setFormat("flv");
        // 设置编码格式
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);

        // 一秒内的帧数
        recorder.setFrameRate(getFrameRate());

        recorder.start();
    }

    @Override
    protected void output(Frame frame) throws Exception {
        if (0L==startRecordTime) {
            startRecordTime = System.currentTimeMillis();
        }

        // 时间戳
        recorder.setTimestamp(1000 * (System.currentTimeMillis()-startRecordTime));

        // 存盘
        recorder.record(frame);
    }

    @Override
    protected void releaseOutputResource() throws Exception {
        recorder.close();
    }

    public static void main(String[] args) {
        new RecordCamera(30.0).action(100);
    }
}