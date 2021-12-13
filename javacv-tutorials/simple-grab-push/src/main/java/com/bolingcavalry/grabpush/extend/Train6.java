package com.bolingcavalry.grabpush.extend;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_face.FaceRecognizer;
import org.bytedeco.opencv.opencv_face.FisherFaceRecognizer;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.CV_32SC1;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

/**
 * @author willzhao
 * @version 1.0
 * @description 训练
 * @date 2021/12/12 18:26
 */
public class Train6 {

    /**
     * 调整后的文件宽度
     */
    public static final int RESIZE_WIDTH = 250;

    /**
     * 调整后的文件高度
     */
    public static final int RESIZE_HEIGHT = 250;

    private static OpenCVFrameConverter.ToMat matConv = new OpenCVFrameConverter.ToMat();

    private static Java2DFrameConverter             biConv  = new Java2DFrameConverter();



    private void train(String dir, String outputPath) throws IOException {
        List<String> subDirs = folderMethod2(dir);




        int totalImageNums = subDirs.size();


        System.out.println("total : " + totalImageNums);


        MatVector imageIndexMatMap = new MatVector(totalImageNums);

        // 这里面用来记录每一张照片的类型
        Mat lables = new Mat(totalImageNums, 1, CV_32SC1);

        IntBuffer lablesBuf = lables.createBuffer();

        int kindIndex = 1;
        int imageIndex = 0;

        for(String subdir : subDirs) {
            List<String> files = folderMethod1(subdir);

            for(String file : files) {
                imageIndexMatMap.put(imageIndex, read(file));
                lablesBuf.put(imageIndex, kindIndex);
                System.out.println(subdir + " - " + kindIndex);
                imageIndex++;
                break;
            }

            kindIndex++;
        }

        FaceRecognizer faceRecognizer = FisherFaceRecognizer.create();

        faceRecognizer.train(imageIndexMatMap, lables);
        faceRecognizer.save(outputPath);
        faceRecognizer.close();
    }

    private Mat read(String path) {
        Mat faceMat = opencv_imgcodecs.imread(path,IMREAD_GRAYSCALE);
//        resize(faceMat, faceMat, new Size(RESIZE_WIDTH, RESIZE_HEIGHT));
        return faceMat;
    }

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

    public static List<String> folderMethod2(String path) {
        List<String> paths = new LinkedList<>();

        File file = new File(path);

        if (file.exists()) {
            File[] files = file.listFiles();

            for (File f : files) {
                if (f.isDirectory()) {
                    System.out.println("文件夹:" + f.getAbsolutePath());
                    paths.add(f.getAbsolutePath());
                } else {
                    System.out.println("文件:" + f.getAbsolutePath());
                }
            }
        }

        return paths;
    }



    public static void main(String[] args) throws IOException {
        String base = "E:\\temp\\202112\\15\\001\\";

        Train6 train = new Train6();
        train.train(base + "lfw\\", base +"faceRecognizer.xml");
    }

}
