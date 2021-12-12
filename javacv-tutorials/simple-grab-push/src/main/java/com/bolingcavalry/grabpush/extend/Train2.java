package com.bolingcavalry.grabpush.extend;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_face.FaceRecognizer;
import org.bytedeco.opencv.opencv_face.FisherFaceRecognizer;

import java.io.IOException;
import java.nio.IntBuffer;

import static org.bytedeco.opencv.global.opencv_core.CV_32SC1;
import static org.bytedeco.opencv.global.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;
import static org.bytedeco.opencv.helper.opencv_imgcodecs.cvLoadImage;

/**
 * @author willzhao
 * @version 1.0
 * @description 训练
 * @date 2021/12/12 18:26
 */
public class Train2 {

    /**
     * 调整后的文件宽度
     */
    public static final int RESIZE_WIDTH = 64;

    /**
     * 调整后的文件高度
     */
    public static final int RESIZE_HEIGHT = 64;

    private static OpenCVFrameConverter.ToMat matConv = new OpenCVFrameConverter.ToMat();
    private static OpenCVFrameConverter.ToIplImage  iplConv = new OpenCVFrameConverter.ToIplImage();

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

        IplImage img;
        IplImage grayImg;

        for(String[] oneOfFaces : sources) {
            for(String face : oneOfFaces) {
                // imageIndex代表第几张，kindIndex代表第几类
                // 将每一张的类型存储在lablesBuf中
                lablesBuf.put(imageIndex, (byte)kindIndex);


                img = cvLoadImage(face);

                grayImg = IplImage.create(img.width(), img.height(), IPL_DEPTH_8U, 1);

                Mat faceMat = matConv.convertToMat(iplConv.convert(grayImg));

                resize(faceMat, faceMat, new Size(122, 122));



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
        String[] kindDirNames = {"AndyLou", "DanielWu"};
        int kindNum = kindDirNames.length;
        int oneKindPicNum = 13;

        String[][] sources = new String[kindNum][oneKindPicNum];

        for(int i=0;i<kindNum;i++) {
            for (int j=0;j<oneKindPicNum;j++) {
                sources[i][j] = base + kindDirNames[i] + "\\" + (j+1) + ".png";
            }
        }

        Train2 train = new Train2();
        train.train(sources, "E:\\temp\\202112\\12\\faceRecognizer3.xml");
    }

}
