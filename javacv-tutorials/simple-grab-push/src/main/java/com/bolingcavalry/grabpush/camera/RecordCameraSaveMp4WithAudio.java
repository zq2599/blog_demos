package com.bolingcavalry.grabpush.camera;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_YUV420P;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2021/11/28 19:26
 * @description 将摄像头数据存储为mp4文件的应用
 */
@Slf4j
public class RecordCameraSaveMp4WithAudio extends AbstractCameraApplication {

    // 存放视频文件的完整位置，请改为自己电脑的可用目录
    private static final String RECORD_FILE_PATH = "E:\\temp\\202111\\28\\camera-"
                                                 + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                                                 + ".mp4";

    final private static int AUDIO_DEVICE_INDEX = 4;

    // 帧录制器
    protected FFmpegFrameRecorder recorder;

    // 定时器
    private ScheduledThreadPoolExecutor exec;

    private void audioData() {
        AudioFormat audioFormat = new AudioFormat(44100.0F, 16, 2, true, false);

        Mixer.Info[] minfoSet = AudioSystem.getMixerInfo();

        Mixer mixer = AudioSystem.getMixer((minfoSet[AUDIO_DEVICE_INDEX]));

        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

        try {
            final TargetDataLine line = (TargetDataLine)AudioSystem.getLine(dataLineInfo);
            line.open(audioFormat);
            line.start();

            final int sampleRate = (int)audioFormat.getSampleRate();
            final int numChannels = audioFormat.getChannels();

            final int audioBufferSize = sampleRate * numChannels;
            final byte[] audioBytes = new byte[audioBufferSize];

            exec = new ScheduledThreadPoolExecutor(1);

            exec.scheduleAtFixedRate((Runnable) new Runnable() {
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
            }, 0, 1000 / (long)getFrameRate(), TimeUnit.MILLISECONDS);
        } catch (Exception exception) {
            log.error("sample error");
        }
    }


    @Override
    protected void initOutput() throws Exception {
        // 实例化FFmpegFrameRecorder
        recorder = new FFmpegFrameRecorder(RECORD_FILE_PATH,        // 存放文件的位置
                                           getCameraImageWidth(),   // 分辨率的宽，与视频源一致
                                           getCameraImageHeight(),  // 分辨率的高，与视频源一致
                              2);                      // 音频通道，2表示立体声

        // 文件格式
        recorder.setFormat("mp4");

        // 帧率与抓取器一致
        recorder.setFrameRate(getFrameRate());

        // 编码格式
        recorder.setPixelFormat(AV_PIX_FMT_YUV420P);

        // 编码器类型
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);

        // 视频质量，0表示无损
        recorder.setVideoQuality(0);

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

        // 初始化
        recorder.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                audioData();
            }
        }).start();
    }

    @Override
    protected void output(Frame frame) throws Exception {
        // 存盘
        recorder.record(frame);
    }

    @Override
    protected void releaseOutputResource() throws Exception {
        // 关闭定时器
        exec.shutdown();
        recorder.close();
    }

    public static void main(String[] args) {
        // 录制30秒视频
        new RecordCameraSaveMp4WithAudio().action(10);
    }
}