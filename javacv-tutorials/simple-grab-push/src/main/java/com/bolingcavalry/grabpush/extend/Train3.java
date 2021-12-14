package com.bolingcavalry.grabpush.extend;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_face.FaceRecognizer;
import org.bytedeco.opencv.opencv_face.FisherFaceRecognizer;

import java.io.IOException;
import java.nio.IntBuffer;

import static org.bytedeco.opencv.global.opencv_core.CV_32SC1;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

/**
 * @author willzhao
 * @version 1.0
 * @description 训练
 * @date 2021/12/12 18:26
 */
public class Train3 {

    /**
     * 调整后的文件宽度
     */
    public static final int RESIZE_WIDTH = 164;

    /**
     * 调整后的文件高度
     */
    public static final int RESIZE_HEIGHT = 164;

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


        imageIndexMatMap.put(0, read(sources[0][0]));
        imageIndexMatMap.put(1, read(sources[0][1]));
        imageIndexMatMap.put(2, read(sources[1][0]));
        imageIndexMatMap.put(3, read(sources[1][1]));
        imageIndexMatMap.put(4, read(sources[2][0]));
        imageIndexMatMap.put(5, read(sources[2][1]));

        lablesBuf.put(0, 11);
        lablesBuf.put(1, 11);
        lablesBuf.put(2, 33);
        lablesBuf.put(3, 33);
        lablesBuf.put(4, 66);
        lablesBuf.put(5, 66);

        /*
        int kindIndex = 0;
        int imageIndex = 0;

        String base = "E:\\temp\\202112\\13\\";

        for(String[] oneOfFaces : sources) {
            for(String face : oneOfFaces) {
                System.out.println("处理第[" + imageIndex + "]张");
                // imageIndex代表第几张，kindIndex代表第几类
                // 将每一张的类型存储在lablesBuf中
                lablesBuf.put(imageIndex, (byte)kindIndex);

                Mat faceMat = opencv_imgcodecs.imread(face,IMREAD_GRAYSCALE);

                resize(faceMat, faceMat, new Size(RESIZE_WIDTH, RESIZE_HEIGHT));

                opencv_imgcodecs.imwrite(base + imageIndex + ".png", faceMat);



                imageIndexMatMap.put(imageIndex, faceMat);

                imageIndex++;
            }

            kindIndex++;
        }
        */


        FaceRecognizer faceRecognizer = FisherFaceRecognizer.create();

        faceRecognizer.train(imageIndexMatMap, lables);
        faceRecognizer.save(outputPath);
        faceRecognizer.close();
    }

    private Mat read(String path) {
        Mat faceMat = opencv_imgcodecs.imread(path,IMREAD_GRAYSCALE);
        resize(faceMat, faceMat, new Size(RESIZE_WIDTH, RESIZE_HEIGHT));
        return faceMat;
    }



    public static void main(String[] args) throws IOException {
        String base = "E:\\temp\\202112\\15\\002\\";
        String[] kindDirNames = {"me", "wife", "dauter"};
        int kindNum = kindDirNames.length;
        int oneKindPicNum = 2;

        String[][] sources = new String[kindNum][oneKindPicNum];

        for(int i=0;i<kindNum;i++) {
            for (int j=0;j<oneKindPicNum;j++) {
                sources[i][j] = base + kindDirNames[i] + "\\" + (j+1) + ".png";
            }
        }

        Train3 train = new Train3();
        train.train(sources, base +"faceRecognizer.xml");
    }

}
