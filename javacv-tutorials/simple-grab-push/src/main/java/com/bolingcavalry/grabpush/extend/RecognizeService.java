package com.bolingcavalry.grabpush.extend;

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
public class RecognizeService {

    /**
     * 调整后的文件宽度
     */
    public static final int RESIZE_WIDTH = 164;

    /**
     * 调整后的文件高度
     */
    public static final int RESIZE_HEIGHT = 164;

    private FaceRecognizer faceRecognizer;

    // 推理结果的标签
    private int[] plabel;

    // 推理结果的置信度
    private double[] pconfidence;

    // 推理结果
    private PredictRlt predictRlt;


    public RecognizeService(String modelPath) {
        plabel = new int[1];
        pconfidence = new double[1];
        predictRlt = new PredictRlt();
        faceRecognizer = FisherFaceRecognizer.create();
        faceRecognizer.read(modelPath);
        faceRecognizer.setThreshold(2000);
    }

    /**
     * 将Mat实例给模型去推理
     * @param mat
     * @return
     */
    public PredictRlt predict(Mat mat) {
        // 调整到和训练一致的尺寸
        resize(mat, mat, new Size(RESIZE_WIDTH, RESIZE_HEIGHT));
        // 推理
        faceRecognizer.predict(mat, plabel, pconfidence);

        predictRlt.setLable(plabel[0]);
        predictRlt.setConfidence(pconfidence[0]);

        return predictRlt;
    }


    private void recog(String recognizerModel, String file) {

        FaceRecognizer faceRecognizer = FisherFaceRecognizer.create();
        faceRecognizer.read(recognizerModel);

        Mat faceMat = opencv_imgcodecs.imread(file,IMREAD_GRAYSCALE);
        resize(faceMat, faceMat, new Size(RESIZE_WIDTH, RESIZE_HEIGHT));

        int[] plabel = new int[1];
        double[] pconfidence = new double[1];

        faceRecognizer.setThreshold(1500);
        faceRecognizer.predict(faceMat, plabel, pconfidence);

        System.out.println("lable [" + plabel[0] + "], confidence [" + pconfidence[0] + "]");
    }

    public static void main(String[] args) {
//        String base = "E:\\temp\\202112\\15\\002\\";
//
//        new PredictService().recog(base + "faceRecognizer.xml", base + "1.png");
    }
}
