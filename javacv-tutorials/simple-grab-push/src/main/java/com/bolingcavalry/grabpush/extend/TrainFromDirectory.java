package com.bolingcavalry.grabpush.extend;

import com.bolingcavalry.grabpush.Constants;
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
public class TrainFromDirectory {

    /**
     * 从指定目录下
     * @param dirs
     * @param outputPath
     * @throws IOException
     */
    private void train(String[] dirs, String outputPath) throws IOException {
        int totalImageNums = 0;

        // 统计每个路径下的照片数，加在一起就是照片总数
        for(String dir : dirs) {
            List<String> files = getAllFilePath(dir);
            totalImageNums += files.size();
        }

        System.out.println("total : " + totalImageNums);

        // 这里用来保存每一张照片的序号，和照片的Mat对象
        MatVector imageIndexMatMap = new MatVector(totalImageNums);

        Mat lables = new Mat(totalImageNums, 1, CV_32SC1);

        // 这里用来保存每一张照片的序号，和照片的类别
        IntBuffer lablesBuf = lables.createBuffer();

        // 类别序号，从1开始，dirs中的每个目录就是一个类别
        int kindIndex = 1;

        // 照片序号，从0开始
        int imageIndex = 0;

        // 每个目录下的照片都遍历
        for(String dir : dirs) {
            // 得到当前目录下所有照片的绝对路径
            List<String> files = getAllFilePath(dir);

            // 处理一个目录下的每张照片，它们的序号不同，类别相同
            for(String file : files) {
                // imageIndexMatMap放的是照片的序号和Mat对象
                imageIndexMatMap.put(imageIndex, read(file));
                // bablesBuf放的是照片序号和类别
                lablesBuf.put(imageIndex, kindIndex);
                // 照片序号加一
                imageIndex++;
            }

            // 每当遍历完一个目录，才会将类别加一
            kindIndex++;
        }

        // 实例化人脸识别类
        FaceRecognizer faceRecognizer = FisherFaceRecognizer.create();
        // 训练，入参就是图片集合和分类集合
        faceRecognizer.train(imageIndexMatMap, lables);
        // 训练完成后，模型保存在指定位置
        faceRecognizer.save(outputPath);
        //释放资源
        faceRecognizer.close();
    }

    /**
     * 读取指定图片的灰度图，调整为指定大小
     * @param path
     * @return
     */
    private static Mat read(String path) {
        Mat faceMat = opencv_imgcodecs.imread(path,IMREAD_GRAYSCALE);
        resize(faceMat, faceMat, new Size(Constants.RESIZE_WIDTH, Constants.RESIZE_HEIGHT));
        return faceMat;
    }

    /**
     * 把指定路径下所有文件的绝对路径放入list集合中返回
     * @param path
     * @return
     */
    public static List<String> getAllFilePath(String path) {
        List<String> paths = new LinkedList<>();

        File file = new File(path);

        if (file.exists()) {
            // 列出该目录下的所有文件
            File[] files = file.listFiles();

            for (File f : files) {
                if (!f.isDirectory()) {
                    // 把每个文件的绝对路径都放在list中
                    paths.add(f.getAbsolutePath());
                }
            }
        }

        return paths;
    }

    public static void main(String[] args) throws IOException {

        String base = "E:\\temp\\202112\\18\\001\\";

        // 存储图片的两个目录
        // man目录下保存了群众演员A的所有人脸照片，
        // woman目录下保存了群众演员B的所有人脸照片
        String[] dirs = {base + "man", base + "woman"};

        // 开始训练，并指定模型输出位置
        new TrainFromDirectory().train(dirs, base + "faceRecognizer.xml");
    }
}
