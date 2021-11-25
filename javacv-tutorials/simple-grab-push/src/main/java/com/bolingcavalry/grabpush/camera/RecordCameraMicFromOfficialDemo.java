package com.bolingcavalry.grabpush.camera;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RecordCameraMicFromOfficialDemo extends AbstractCameraApplication {

//    private static final String RECORD_ADDRESS = "/Users/zhaoqin/temp/202111/20/camera.mp4";
    private static final String RECORD_ADDRESS = "rtmp://192.168.50.43:11935/live/livestream";

    private static final int AUDIO_DEVICE_INDEX = 4;

    protected FFmpegFrameRecorder recorder;

    protected long startRecordTime = 0L;

    public RecordCameraMicFromOfficialDemo(double frameRate) {
        super(frameRate);
    }


    private void initAndStartAudioGrab() {
        AudioFormat audioFormat = new AudioFormat(44100.0F, 16, 2, true, false);

        Mixer.Info[] minfoSet = AudioSystem.getMixerInfo();
        Mixer mixer = AudioSystem.getMixer(minfoSet[AUDIO_DEVICE_INDEX]);

        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

        try {
            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
            line.open(audioFormat);
            line.start();

            int sampleRate = (int)audioFormat.getSampleRate();
            int numChannels = audioFormat.getChannels();
            int audioBufferSize = sampleRate * numChannels;
            byte[] audioBytes = new byte[audioBufferSize];

            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

            executor.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            int nBytesRead = 0;
                            while (nBytesRead == 0) {
                                nBytesRead = line.read(audioBytes, 0, line.available());
                            }

                            // Since we specified 16 bits in the AudioFormat,
                            // we need to convert our read byte[] to short[]
                            // (see source from FFmpegFrameRecorder.recordSamples for AV_SAMPLE_FMT_S16)
                            // Let's initialize our short[] array
                            int nSamplesRead = nBytesRead / 2;
                            short[] samples = new short[nSamplesRead];

                            // Let's wrap our short[] into a ShortBuffer and
                            // pass it to recordSamples
                            ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
                            ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);

                            // recorder is instance of
                            // org.bytedeco.javacv.FFmpegFrameRecorder
                            recorder.recordSamples(sampleRate, numChannels, sBuff);
                        } catch (Exception exception) {
                            log.error("get samples error", exception);
                        }
                    }
                },
                0,
                (long)(1000/getFrameRate()),
                TimeUnit.SECONDS);
        }  catch (Exception exception) {
            log.error("scheduleAtFixedRate error", exception);
        }
    }

    @Override
    protected void initOutput() throws Exception {
        // 实例化FFmpegFrameRecorder，将SRS的推送地址传入
        recorder = new FFmpegFrameRecorder(RECORD_ADDRESS, getCameraImageWidth(), getCameraImageHeight(), 2);
        recorder.setInterleaved(true);

        recorder.setVideoOption("tune", "zerolatency");
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setVideoOption("crf", "25");
        recorder.setVideoBitrate(2000000);

        // 设置封装格式
        recorder.setFormat("flv");
        // 设置编码格式
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);

        // 一秒内的帧数
        recorder.setFrameRate(getFrameRate());

        // 关键帧间隔，一般是视频帧率的两倍
        recorder.setGopSize((int)(getFrameRate()*2));

        recorder.setAudioOption("crf", "0");

        recorder.setAudioQuality(0);
        recorder.setAudioBitrate(128000);
//        recorder.setAudioBitrate(192000);
        recorder.setSampleRate(44100);
        recorder.setAudioChannels(2);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.start();

        // 在一个新线程初始化和启动音频采样
        new Thread(new Runnable() {
            @Override
            public void run() {
                initAndStartAudioGrab();
            }
        }).start();
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
        new RecordCameraMicFromOfficialDemo(25).action(1000);
    }
}