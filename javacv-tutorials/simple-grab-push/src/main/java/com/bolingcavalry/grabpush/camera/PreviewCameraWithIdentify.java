package com.bolingcavalry.grabpush.camera;

import com.bolingcavalry.grabpush.extend.DetectAndRecognizeService;
import com.bolingcavalry.grabpush.extend.DetectService;
import com.bolingcavalry.grabpush.extend.HaarCascadeDetectService;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class PreviewCameraWithIdentify extends AbstractCameraApplication {

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
    public PreviewCameraWithIdentify(DetectService detectService) {
        this.detectService = detectService;
    }

    @Override
    protected void initOutput() throws Exception {
        previewCanvas = new CanvasFrame("摄像头预览和身份识别", CanvasFrame.getDefaultGamma() / grabber.getGamma());
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
        String modelFileUrl = "https://raw.github.com/opencv/opencv/master/data/haarcascades/haarcascade_frontalface_alt.xml";
        String recognizeModelFilePath = "E:\\temp\\202112\\18\\001\\faceRecognizer.xml";

        // 这里分类编号的身份的对应关系，和之前训练时候的设定要保持一致
        Map<Integer, String> kindNameMap = new HashMap();
        kindNameMap.put(1, "Man");
        kindNameMap.put(2, "Woman");

        // 检测服务
        DetectService detectService = new DetectAndRecognizeService(modelFileUrl,recognizeModelFilePath, kindNameMap);

        // 开始检测
        new PreviewCameraWithIdentify(detectService).action(1000);
    }
}