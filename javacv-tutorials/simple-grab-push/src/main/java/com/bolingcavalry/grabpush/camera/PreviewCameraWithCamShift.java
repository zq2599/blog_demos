package com.bolingcavalry.grabpush.camera;

import com.bolingcavalry.grabpush.extend.CamShiftDetectService;
import com.bolingcavalry.grabpush.extend.DetectService;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.opencv.core.Core;

import javax.swing.*;

@Slf4j
public class PreviewCameraWithCamShift extends AbstractCameraApplication {
    // 静态代码块定义，会在程序开始运行时先被调用初始化
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME); // 得保证先执行该语句，用于加载库，才能调用其他操作库的语句，
    }

    /**
     * 本机窗口
     */
    protected CanvasFrame previewCanvas;

    /**
     * 检测工具接口
     */
    private DetectService detectService;

    /**
     * 不同的检测工具，可以通过构造方法传入
     * @param detectService
     */
    public PreviewCameraWithCamShift(DetectService detectService) {
        this.detectService = detectService;
    }

    @Override
    protected void initOutput() throws Exception {
        previewCanvas = new CanvasFrame("摄像头预览", CanvasFrame.getDefaultGamma() / grabber.getGamma());
        previewCanvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        previewCanvas.setAlwaysOnTop(true);

        // 检测服务的初始化操作
        detectService.init();
    }

    @Override
    protected void output(Frame frame) {
        // 原始帧先交给检测服务处理，这个处理包括物体检测，再将检测结果标注在原始图片上，
        // 然后转换为帧返回
        Frame detectedFrame = detectService.convert(frame);
        // 预览窗口上显示的帧是标注了检测结果的帧
        previewCanvas.showImage(detectedFrame);
    }

    @Override
    protected void releaseOutputResource() {
        if (null!= previewCanvas) {
            previewCanvas.dispose();
        }

        // 检测工具也要释放资源
        detectService.releaseOutputResource();
    }

    @Override
    protected int getInterval() {
        return super.getInterval()/8;
    }

    public static void main(String[] args) {
        String modelFilePath = System.getProperty("model.file.path");
        log.info("模型文件本地路径：{}", modelFilePath);
        new PreviewCameraWithCamShift(new CamShiftDetectService(modelFilePath)).action(1000);
    }
}