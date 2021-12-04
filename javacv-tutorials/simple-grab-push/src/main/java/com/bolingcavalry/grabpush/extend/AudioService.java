package com.bolingcavalry.grabpush.extend;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameRecorder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author willzhao
 * @version 1.0
 * @description 音频相关的服务
 * @date 2021/12/3 8:09
 */
@Slf4j
public class AudioService {

    // 采样率
    private final static int SAMPLE_RATE = 44100;

    // 音频通道数，2表示立体声
    private final static int CHANNEL_NUM = 2;

    // 帧录制器
    private FFmpegFrameRecorder recorder;

    // 定时器
    private ScheduledThreadPoolExecutor sampleTask;

    // 目标数据线，音频数据从这里获取
    private TargetDataLine line;

    // 该数组用于保存从数据线中取得的音频数据
    byte[] audioBytes;

    // 定时任务的线程中会读此变量，而改变此变量的值是在主线程中，因此要用volatile保持可见性
    private volatile boolean isFinish = false;

    /**
     * 帧录制器的音频参数设置
     * @param recorder
     * @throws Exception
     */
    public void setRecorderParams(FrameRecorder recorder) throws Exception {
        this.recorder = (FFmpegFrameRecorder)recorder;

        // 码率恒定
        recorder.setAudioOption("crf", "0");
        // 最高音质
        recorder.setAudioQuality(0);
        // 192 Kbps
        recorder.setAudioBitrate(192000);

        // 采样率
        recorder.setSampleRate(SAMPLE_RATE);

        // 立体声
        recorder.setAudioChannels(2);
        // 编码器
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
    }

    /**
     * 音频采样对象的初始化
     * @throws Exception
     */
    public void initSampleService() throws Exception {
        // 音频格式的参数
        AudioFormat audioFormat = new AudioFormat(SAMPLE_RATE, 16, CHANNEL_NUM, true, false);

        // 获取数据线所需的参数
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

        // 从音频捕获设备取得其数据的数据线，之后的音频数据就从该数据线中获取
        line = (TargetDataLine)AudioSystem.getLine(dataLineInfo);

        line.open(audioFormat);

        // 数据线与音频数据的IO建立联系
        line.start();

        // 每次取得的原始数据大小
        final int audioBufferSize = SAMPLE_RATE * CHANNEL_NUM;

        // 初始化数组，用于暂存原始音频采样数据
        audioBytes = new byte[audioBufferSize];

        // 创建一个定时任务，任务的内容是定时做音频采样，再把采样数据交给帧录制器处理
        sampleTask = new ScheduledThreadPoolExecutor(1);
    }

    /**
     * 程序结束前，释放音频相关的资源
     */
    public void releaseOutputResource() {
        // 结束的标志，避免采样的代码在whlie循环中不退出
        isFinish = true;
        // 结束定时任务
        sampleTask.shutdown();
        // 停止数据线
        line.stop();
        // 关闭数据线
        line.close();
    }

    /**
     * 启动定时任务，每秒执行一次，采集音频数据给帧录制器
     * @param frameRate
     */
    public void startSample(double frameRate) {

        // 启动定时任务，每秒执行一次，采集音频数据给帧录制器
        sampleTask.scheduleAtFixedRate((Runnable) new Runnable() {
            @Override
            public void run() {
                try
                {
                    int nBytesRead = 0;

                    while (nBytesRead == 0 && !isFinish) {
                        // 音频数据是从数据线中取得的
                        nBytesRead = line.read(audioBytes, 0, line.available());
                    }

                    // 如果nBytesRead<1，表示isFinish标志被设置true，此时该结束了
                    if (nBytesRead<1) {
                        return;
                    }

                    // 采样数据是16比特，也就是2字节，对应的数据类型就是short，
                    // 所以准备一个short数组来接受原始的byte数组数据
                    // short是2字节，所以数组长度就是byte数组长度的二分之一
                    int nSamplesRead = nBytesRead / 2;
                    short[] samples = new short[nSamplesRead];

                    // 两个byte放入一个short中的时候，谁在前谁在后？这里用LITTLE_ENDIAN指定拜访顺序，
                    ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
                    // 将short数组转为ShortBuffer对象，因为帧录制器的入参需要该类型
                    ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);

                    // 音频帧交给帧录制器输出
                    recorder.recordSamples(SAMPLE_RATE, CHANNEL_NUM, sBuff);
                }
                catch (FrameRecorder.Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000 / (long)frameRate, TimeUnit.MILLISECONDS);
    }
}
