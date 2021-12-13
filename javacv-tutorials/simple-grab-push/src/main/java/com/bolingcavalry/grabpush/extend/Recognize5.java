package com.bolingcavalry.grabpush.extend;

import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_face.FaceRecognizer;
import org.bytedeco.opencv.opencv_face.FisherFaceRecognizer;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2021/12/12 21:32
 */
public class Recognize5 {

    /**
     * 调整后的文件宽度
     */
    public static final int RESIZE_WIDTH = 128;

    /**
     * 调整后的文件高度
     */
    public static final int RESIZE_HEIGHT = 128;

    public static List<String> folderMethod1(String path) {
        List<String> paths = new LinkedList<>();

        File file = new File(path);

        if (file.exists()) {
            File[] files = file.listFiles();

            for (File f : files) {
                if (f.isDirectory()) {
                    System.out.println("文件夹:" + f.getAbsolutePath());
                } else {
                    System.out.println("文件:" + f.getAbsolutePath());

                    paths.add(f.getAbsolutePath());
                }
            }
        }

        return paths;
    }

    private void recog(String recognizerModel, String dir) {

        FaceRecognizer faceRecognizer = FisherFaceRecognizer.create();
        faceRecognizer.read(recognizerModel);
        faceRecognizer.setThreshold(3000);

        List<String> files = folderMethod1(dir);

        int right = 0;
        int wrong = 0;

        for (String file : files) {
            Mat faceMat = opencv_imgcodecs.imread(file,IMREAD_GRAYSCALE);
            resize(faceMat, faceMat, new Size(RESIZE_WIDTH, RESIZE_HEIGHT));
            int[] plabel = new int[1];
            double[] pconfidence = new double[1];
            faceRecognizer.predict(faceMat, plabel, pconfidence);

            if (2==plabel[0]) {
                right++;
            } else {
                wrong++;
            }
            System.out.println("lable [" + plabel[0] + "], confidence [" + pconfidence[0] + "]");
        }

        System.out.println("right [" + right + "], wrong [" + wrong + "]");
    }

    public static void main(String[] args) {
        String base = "E:\\temp\\202112\\14\\";

        new Recognize5().recog(base + "faceRecognizer.xml", base + "liu-test");
    }
}
