package com.bolingcavalry.grabpush.extend;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.DoublePointer;

import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.*;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_face.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2021/12/12 21:32
 */
public class Recognize {


    private void recog(String recognizerModel, String file) {

        FaceRecognizer faceRecognizer = FisherFaceRecognizer.create();
        faceRecognizer.read(recognizerModel);

        IntPointer label = new IntPointer(1);
        DoublePointer confidence = new DoublePointer(1);


        Mat faceMat = opencv_imgcodecs.imread(file,IMREAD_GRAYSCALE);
        resize(faceMat, faceMat, new Size(122, 122));

        int[] plabel = new int[1];
        double[] pconfidence = new double[1];

        faceRecognizer.predict(faceMat, plabel, pconfidence);

        System.out.println("lable [" + plabel[0] + "], confidence [" + pconfidence[0] + "]");
    }

    public static void main(String[] args) {
        String base = "E:\\temp\\202112\\12\\";

        new Recognize().recog(base + "faceRecognizer2.xml", base + "c.png");
    }
}
