package com.bolingcavalry.grabpush.camera;

import com.bolingcavalry.grabpush.plugin.OutputPlugin;
import com.bolingcavalry.grabpush.plugin.impl.AudioOutputPlugin;
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
 * @description 将摄像头数据存储为mp4文件的应用
 */
@Slf4j
public class RecordCameraSaveMp4 extends AbstractCameraApplication {

    public RecordCameraSaveMp4() {
        super();
    }

    public RecordCameraSaveMp4(OutputPlugin outputPlugin) {
        this.outputPlugin = outputPlugin;
    }

    // 存放视频文件的完整位置，请改为自己电脑的可用目录
    private static final String RECORD_FILE_PATH = "E:\\temp\\202111\\28\\camera-"
                                                 + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                                                 + ".mp4";

    // 帧录制器
    protected FrameRecorder recorder;

    private OutputPlugin outputPlugin;

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

        // 如果有插件，就执行插件的初始化操作
        if (null!=outputPlugin) {
            outputPlugin.doBeforeStart(recorder, RecordCameraSaveMp4.this);
        }

        // 初始化
        recorder.start();

        // 如果有插件，就执行插件的后初始化操作
        if (null!=outputPlugin) {
            outputPlugin.doAfterStart();
        }
    }

    @Override
    protected void output(Frame frame) throws Exception {
        // 存盘
        recorder.record(frame);

        // 如果有插件，就执行插件的输出操作
        if (null!=outputPlugin) {
            outputPlugin.output(frame);
        }
    }

    @Override
    protected void releaseOutputResource() throws Exception {
        // 如果有插件，就执行插件的资源释放操作
        if (null!=outputPlugin) {
            outputPlugin.releaseOutputResource();
        }

        recorder.close();
    }

    public static void main(String[] args) {
        // 录制30秒视频
        new RecordCameraSaveMp4().action(30);
//        new RecordCameraSaveMp4(new AudioOutputPlugin()).action(30);
    }
}