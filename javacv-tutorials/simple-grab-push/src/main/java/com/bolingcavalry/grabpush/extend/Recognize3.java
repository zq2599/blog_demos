package com.bolingcavalry.grabpush.extend;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_face.FaceRecognizer;
import org.bytedeco.opencv.opencv_face.FisherFaceRecognizer;

import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2021/12/12 21:32
 */
public class Recognize3 {

    /**
     * 调整后的文件宽度
     */
    public static final int RESIZE_WIDTH = 128;

    /**
     * 调整后的文件高度
     */
    public static final int RESIZE_HEIGHT = 128;

    private void recog(String recognizerModel, String file) {

        FaceRecognizer faceRecognizer = FisherFaceRecognizer.create();
        faceRecognizer.read(recognizerModel);

        Mat faceMat = opencv_imgcodecs.imread(file,IMREAD_GRAYSCALE);
        resize(faceMat, faceMat, new Size(RESIZE_WIDTH, RESIZE_HEIGHT));

        int[] plabel = new int[1];
        double[] pconfidence = new double[1];

        faceRecognizer.setThreshold(0);
        faceRecognizer.predict(faceMat, plabel, pconfidence);

        System.out.println("lable [" + plabel[0] + "], confidence [" + pconfidence[0] + "]");
    }

    public static void main(String[] args) {
        String base = "E:\\temp\\202112\\14\\";

        new Recognize3().recog(base + "faceRecognizer.xml", base + "1.jpg");
    }
}
