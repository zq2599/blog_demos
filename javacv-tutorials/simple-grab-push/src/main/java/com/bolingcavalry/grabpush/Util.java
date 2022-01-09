package com.bolingcavalry.grabpush;

import org.bytedeco.opencv.opencv_core.Mat;

import static org.bytedeco.opencv.global.opencv_core.CV_8UC4;
import static org.bytedeco.opencv.global.opencv_imgproc.CV_BGR2RGBA;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;

/**
 * @author willzhao
 * @version 1.0
 * @description 常用工具类
 * @date 2022/1/9 11:17
 */
public class Util {

    /**
     * 根据传入的MAT构造相同尺寸的MAT，存放RGBA图片用于以后的检测
     * @param src 原始图片的MAT对象
     * @return 相同尺寸的RGBA图片的MAT对象
     */
    public static Mat buildRgbaImage(Mat src) {
        return new Mat(src.rows(), src.cols(), CV_8UC4);
    }

    /**
     * 将javacv的BGR实例转为javacv的RGBA格式，
     * 再将javacv的RGBA实例转为opencv的Mat
     * @param javacvBGR  原始的javacv格式的BGR实例
     * @param javacvRGBA 存放转换成javacv格式RGBA实例的对象
     * @return
     */
    public static org.opencv.core.Mat buildJavacvBGR2OpenCVRGBA(Mat javacvBGR, Mat javacvRGBA) {
        cvtColor(javacvBGR, javacvRGBA, CV_BGR2RGBA);
        return new org.opencv.core.Mat(javacvRGBA.address());
    }
}
