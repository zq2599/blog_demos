package com.bolingcavalry.grabpush.camera;

import com.bolingcavalry.grabpush.extend.AudioService;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_YUV420P;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2021/11/28 19:26
 * @description 将摄像头和麦克风数据存储为mp4文件的应用
 */
@Slf4j
public class RecordCameraSaveMp4WithAudio extends AbstractCameraApplication {

    // 存放视频文件的完整位置，请改为自己电脑的可用目录
    private static final String RECORD_FILE_PATH = "E:\\temp\\202111\\28\\camera-"
                                                 + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                                                 + ".mp4";

    // 帧录制器
    protected FrameRecorder recorder;

    // 音频服务类
    private AudioService audioService = new AudioService();

    @Override
    protected void initOutput() throws Exception {
        // 实例化FFmpegFrameRecorder
        recorder = new FFmpegFrameRecorder(RECORD_FILE_PATH,        // 存放文件的位置
                                           getCameraImageWidth(),   // 分辨率的宽，与视频源一致
                                           getCameraImageHeight(),  // 分辨率的高，与视频源一致
                                            0);                      // 音频通道，0表示无

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

        // 设置帧录制器的音频相关参数
        audioService.setRecorderParams(recorder);

        // 音频采样相关的初始化操作
        audioService.initSampleService();

        // 初始化
        recorder.start();

        // 启动定时任务，采集音频帧给帧录制器
        audioService.startSample(getFrameRate());
    }

    @Override
    protected void output(Frame frame) throws Exception {
        // 存盘
        recorder.record(frame);
    }

    @Override
    protected void releaseOutputResource() throws Exception {
        // 执行音频服务的资源释放操作
        audioService.releaseOutputResource();

        // 关闭帧录制器
        recorder.close();
    }

    public static void main(String[] args) {
        // 录制30秒视频
        new RecordCameraSaveMp4WithAudio().action(30);
    }
}