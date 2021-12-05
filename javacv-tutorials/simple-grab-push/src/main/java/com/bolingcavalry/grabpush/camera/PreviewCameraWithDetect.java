package com.bolingcavalry.grabpush.camera;

import com.bolingcavalry.grabpush.extend.DetectService;
import com.bolingcavalry.grabpush.extend.HaarCascadeDetectService;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;

import javax.swing.*;

@Slf4j
public class PreviewCameraWithDetect extends AbstractCameraApplication {

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
    public PreviewCameraWithDetect(DetectService detectService) {
        this.detectService = detectService;
    }

    @Override
    protected void initOutput() throws Exception {
        previewCanvas = new CanvasFrame("摄像头预览", CanvasFrame.getDefaultGamma() / grabber.getGamma());
        previewCanvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        previewCanvas.setAlwaysOnTop(true);

        detectService.init();
    }

    @Override
    protected void output(Frame frame) {
        // 预览窗口上显示当前帧
        previewCanvas.showImage(detectService.convert(frame));
    }

    @Override
    protected void releaseOutputResource() {
        if (null!= previewCanvas) {
            previewCanvas.dispose();
        }

        // 检测工具也要释放资源
        detectService.releaseOutputResource();
    }

    public static void main(String[] args) {
//        String modelPath = "https://raw.github.com/opencv/opencv/master/data/haarcascades/haarcascade_frontalface_alt.xml";
        String modelPath = "https://raw.github.com/opencv/opencv/master/data/haarcascades/haarcascade_upperbody.xml";
        new PreviewCameraWithDetect(new HaarCascadeDetectService(modelPath)).action(1000);
    }
}