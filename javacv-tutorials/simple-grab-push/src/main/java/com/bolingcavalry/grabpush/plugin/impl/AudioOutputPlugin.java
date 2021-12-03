package com.bolingcavalry.grabpush.plugin.impl;

import com.bolingcavalry.grabpush.camera.AbstractCameraApplication;
import com.bolingcavalry.grabpush.plugin.OutputPlugin;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2021/12/3 8:09
 */
@Slf4j
public class AudioOutputPlugin implements OutputPlugin {

    // 帧录制器
    private FFmpegFrameRecorder recorder;

    // 定时器
    private ScheduledThreadPoolExecutor sampleTask;

    // 帧率
    private double frameRate;

    @Override
    public void doBeforeStart(FrameRecorder recorder, AbstractCameraApplication cameraApplication) throws Exception {
        if (recorder instanceof FFmpegFrameRecorder) {
            this.recorder = (FFmpegFrameRecorder)recorder;
        } else {
            // 如果类型不匹配就抛出异常中断执行
            log.error("FrameRecorder必须是FFmpegFrameRecorder");
            throw new Exception("FrameRecorder必须是FFmpegFrameRecorder");
        }

        // 码率恒定
        recorder.setAudioOption("crf", "0");
        // 最高音质
        recorder.setAudioQuality(0);
        // 192 Kbps
        recorder.setAudioBitrate(192000);

        // 采样率
        recorder.setSampleRate(44100);

        // 立体声
        recorder.setAudioChannels(2);
        // 编码器
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);

        // 帧率稍后会用到，所以保存在成员变量中
        this.frameRate = cameraApplication.getFrameRate();
    }

    @Override
    public void output(Frame frame) {
        // 啥也不用做
    }

    @Override
    public void releaseOutputResource() {
        // 结束定时任务
        sampleTask.shutdown();
    }

    @Override
    public void doAfterStart() {
        // 启动一个线程：先做音频初始化操作，再启动定时任务音频采样送入帧录制器
        new Thread(() -> audioData()).start();
    }

    /**
     * 启动定时任务执行音频采样，将数据交给帧录制器
     */
    private void audioData() {
        AudioFormat audioFormat = new AudioFormat(44100.0F, 16, 2, true, false);

        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

        try {
            final TargetDataLine line = (TargetDataLine)AudioSystem.getLine(dataLineInfo);
            line.open(audioFormat);
            line.start();

            final int sampleRate = (int)audioFormat.getSampleRate();
            final int numChannels = audioFormat.getChannels();

            final int audioBufferSize = sampleRate * numChannels;
            final byte[] audioBytes = new byte[audioBufferSize];

            sampleTask = new ScheduledThreadPoolExecutor(1);

            sampleTask.scheduleAtFixedRate((Runnable) new Runnable() {
                @Override
                public void run()
                {
                    try
                    {
                        // Read from the line... non-blocking
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
                    }
                    catch (FrameRecorder.Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }, 0, 1000 / (long)frameRate, TimeUnit.MILLISECONDS);
        } catch (Exception exception) {
            log.error("sample error");
        }
    }
}
