package com.bolingcavalry.grabpush.camera;

import com.bolingcavalry.grabpush.extend.AgeDetectService;
import com.bolingcavalry.grabpush.extend.DetectService;
import com.bolingcavalry.grabpush.extend.GenderDetectService;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;

import javax.swing.*;

@Slf4j
public class PreviewCameraWithGenderAge extends AbstractCameraApplication {

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
    public PreviewCameraWithGenderAge(DetectService detectService) {
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
        String base = "E:\\temp\\202112\\25\\opencv\\";
        /*
        DetectService detectService = new GenderDetectService(
                base + "haarcascade_frontalface_alt.xml",
                base + "gender\\deploy.prototxt",
                base + "gender\\gender_net.caffemodel");

        */

        DetectService detectService = new AgeDetectService(
                base + "haarcascade_frontalface_alt.xml",
                base + "age\\deploy.prototxt",
                base + "age\\age_net.caffemodel");

        new PreviewCameraWithGenderAge(detectService).action(1000);
    }
}