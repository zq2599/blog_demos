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

    public RecordCamera(double frameRate, int cameraImageWidth, int cameraImageHeight) {
        super(frameRate, cameraImageWidth, cameraImageHeight);
    }

    @Override
    protected void initOutput() throws Exception {
        // 实例化FFmpegFrameRecorder，将SRS的推送地址传入
        recorder = FrameRecorder.createDefault(RECORD_ADDRESS, getCameraImageWidth(), getCameraImageHeight());

        // decrease "startup" latency in FFMPEG (see:
        // https://trac.ffmpeg.org/wiki/StreamingGuide)
        recorder.setVideoOption("tune", "zerolatency");
        // tradeoff between quality and encode speed
        // possible values are ultrafast,superfast, veryfast, faster, fast,
        // medium, slow, slower, veryslow
        // ultrafast offers us the least amount of compression (lower encoder
        // CPU) at the cost of a larger stream size
        // at the other end, veryslow provides the best compression (high
        // encoder CPU) while lowering the stream size
        // (see: https://trac.ffmpeg.org/wiki/Encode/H.264)
        recorder.setVideoOption("preset", "ultrafast");
        // Constant Rate Factor (see: https://trac.ffmpeg.org/wiki/Encode/H.264)
        recorder.setVideoOption("crf", "28");
        // 2000 kb/s, reasonable "sane" area for 720
        recorder.setVideoBitrate(2000000);

        // 设置编码格式
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);

        // 设置封装格式
        recorder.setFormat("flv");

        // FPS (frames per second)
        // 一秒内的帧数
        recorder.setFrameRate(getFrameRate());
        // Key frame interval, in our case every 2 seconds -> 30 (fps) * 2 = 60
        // (gop length)
        recorder.setGopSize((int)getFrameRate()*2);

        /*
        // We don't want variable bitrate audio
        recorder.setAudioOption("crf", "0");
        // Highest quality
        recorder.setAudioQuality(0);
        // 192 Kbps
        recorder.setAudioBitrate(192000);
        recorder.setSampleRate(44100);
        recorder.setAudioChannels(2);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        */


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

    @Override
    protected int getInterval() {
        return 0;
    }

    public static void main(String[] args) {
        new RecordCamera(30.0,1280,720).action(1000);
    }
}