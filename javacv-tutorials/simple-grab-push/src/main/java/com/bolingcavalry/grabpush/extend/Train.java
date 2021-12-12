package com.bolingcavalry.grabpush.extend;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.IntBuffer;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.DoublePointer;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.*;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_face.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

/**
 * @author willzhao
 * @version 1.0
 * @description 训练
 * @date 2021/12/12 18:26
 */
public class Train {

    /**
     * 调整后的文件宽度
     */
    public static final int RESIZE_WIDTH = 122;

    /**
     * 调整后的文件高度
     */
    public static final int RESIZE_HEIGHT = 122;

    private static OpenCVFrameConverter.ToMat matConv = new OpenCVFrameConverter.ToMat();

    private static Java2DFrameConverter             biConv  = new Java2DFrameConverter();


    private void train(String[][] sources, String outputPath) throws IOException {
        int totalImageNums = 0;

        for (String[] oneKindFaces : sources) {
            totalImageNums += oneKindFaces.length;
        }

        MatVector imageIndexMatMap = new MatVector(totalImageNums);

        // 这里面用来记录每一张照片的类型
        Mat lables = new Mat(totalImageNums, 1, CV_32SC1);

        IntBuffer lablesBuf = lables.createBuffer();

        int kindIndex = 1;
        int imageIndex = 0;

        for(String[] oneOfFaces : sources) {
            for(String face : oneOfFaces) {
                // imageIndex代表第几张，kindIndex代表第几类
                // 将每一张的类型存储在lablesBuf中
                lablesBuf.put(imageIndex, (byte)kindIndex);

                Mat faceMat = opencv_imgcodecs.imread(face,IMREAD_GRAYSCALE);

                resize(faceMat, faceMat, new Size(224, 224));





                imageIndexMatMap.put(imageIndex, faceMat);

                imageIndex++;
            }

            kindIndex++;
        }


        FaceRecognizer faceRecognizer = FisherFaceRecognizer.create();

        faceRecognizer.train(imageIndexMatMap, lables);
        faceRecognizer.save(outputPath);
        faceRecognizer.close();
    }



    public static void main(String[] args) throws IOException {
        String base = "E:\\temp\\202112\\12\\";
        String[] kindDirNames = {"liu", "zhang", "guo", "li"};
        int kindNum = kindDirNames.length;
        int oneKindPicNum = 13;

        String[][] sources = new String[kindNum][oneKindPicNum];

        for(int i=0;i<kindNum;i++) {
            for (int j=0;j<oneKindPicNum;j++) {
                sources[i][j] = base + kindDirNames[i] + "\\" + (j+1) + ".png";
            }
        }

        Train train = new Train();
        train.train(sources, "E:\\temp\\202112\\12\\faceRecognizer.xml");
    }

}
