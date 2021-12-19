package com.bolingcavalry.grabpush.extend;

import com.bolingcavalry.grabpush.Constants;
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
 * @description 把人脸识别的服务集中在这里
 * @date 2021/12/12 21:32
 */
public class RecognizeService {

    private FaceRecognizer faceRecognizer;

    // 推理结果的标签
    private int[] plabel;

    // 推理结果的置信度
    private double[] pconfidence;

    // 推理结果
    private PredictRlt predictRlt;

    // 用于推理的图片尺寸，要和训练时的尺寸保持一致
    private Size size= new Size(Constants.RESIZE_WIDTH, Constants.RESIZE_HEIGHT);


    public RecognizeService(String modelPath) {
        plabel = new int[1];
        pconfidence = new double[1];
        predictRlt = new PredictRlt();

        // 识别类的实例化，与训练时相同
        faceRecognizer = FisherFaceRecognizer.create();
        // 加载的是训练时生成的模型
        faceRecognizer.read(modelPath);
        // 设置门限，这个可以根据您自身的情况不断调整
        faceRecognizer.setThreshold(Constants.MAX_CONFIDENCE);
    }

    /**
     * 将Mat实例给模型去推理
     * @param mat
     * @return
     */
    public PredictRlt predict(Mat mat) {
        // 调整到和训练一致的尺寸
        resize(mat, mat, size);

        boolean isFinish = false;

        try {
            // 推理(这一行可能抛出RuntimeException异常，因此要补货，否则会导致程序退出)
            faceRecognizer.predict(mat, plabel, pconfidence);
            isFinish = true;
        } catch (RuntimeException runtimeException) {
            runtimeException.printStackTrace();
        }

        // 如果发生过异常，就提前返回
        if (!isFinish) {
            return null;
        }

        // 将推理结果写入返回对象中
        predictRlt.setLable(plabel[0]);
        predictRlt.setConfidence(pconfidence[0]);

        return predictRlt;
    }
}
