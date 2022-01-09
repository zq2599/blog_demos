package com.bolingcavalry.grabpush.extend;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;
import java.util.Collections;
import java.util.List;
import java.util.Vector;


/**
 * @author willzhao
 * @version 1.0
 * @description TODO
 * @date 2022/1/8 21:21
 */
public class ObjectTracker {

    private Mat hsv, hue, mask, prob;
    private Rect trackRect;
    private RotatedRect rotatedRect;
    private Mat hist;
    private List<Mat> hsvList, hueList;
    private MatOfFloat ranges;

    public ObjectTracker(Mat rgba) {
        hist = new Mat();
        trackRect = new Rect();
        rotatedRect = new RotatedRect();
        hsvList = new Vector<>();
        hueList = new Vector<>();

        hsv = new Mat(rgba.size(), CvType.CV_8UC3);
        mask = new Mat(rgba.size(), CvType.CV_8UC1);
        hue = new Mat(rgba.size(), CvType.CV_8UC1);

        prob = new Mat(rgba.size(), CvType.CV_8UC1);

        ranges = new MatOfFloat(0f, 256f);
    }

    private void rgba2Hsv(Mat rgba) {

        Imgproc.cvtColor(rgba, hsv, Imgproc.COLOR_RGB2HSV);

        //inRange函数的功能是检查输入数组每个元素大小是否在2个给定数值之间，可以有多通道,mask保存0通道的最小值，也就是h分量
        //这里利用了hsv的3个通道，比较h,0~180,s,smin~256,v,min(vmin,vmax),max(vmin,vmax)。如果3个通道都在对应的范围内，则
        //mask对应的那个点的值全为1(0xff)，否则为0(0x00).
        int vMin = 65, vMax = 256, sMin = 55;
        Core.inRange(
                hsv,
                new Scalar(0, sMin, Math.min(vMin, vMax)),
                new Scalar(180, 256, Math.max(vMin, vMax)),
                mask
        );
    }

    private void updateHueImage() {
        hsvList.clear();
        hsvList.add(hsv);

        // hue初始化为与hsv大小深度一样的矩阵，色调的度量是用角度表示的，红绿蓝之间相差120度，反色相差180度
        hue.create(hsv.size(), hsv.depth());

        hueList.clear();
        hueList.add(hue);
        MatOfInt from_to = new MatOfInt(0, 0);

        // 将hsv第一个通道(也就是色调)的数复制到hue中，0索引数组
        Core.mixChannels(hsvList, hueList, from_to);
    }

    public void createTrackedObject(Mat mRgba, Rect region) {

        //将rgb摄像头帧转化成hsv空间的
        rgba2Hsv(mRgba);

        updateHueImage();

        Mat tempMask = mask.submat(region);

        // MatOfFloat ranges = new MatOfFloat(0f, 256f);
        MatOfInt histSize = new MatOfInt(25);

        List<Mat> images = Collections.singletonList(hueList.get(0).submat(region));
        Imgproc.calcHist(images, new MatOfInt(0), tempMask, hist, histSize, ranges);

        // 将hist矩阵进行数组范围归一化，都归一化到0~255
        Core.normalize(hist, hist, 0, 255, Core.NORM_MINMAX);
        trackRect = region;

    }

    public RotatedRect objectTracking(Mat mRgba) {

        rgba2Hsv(mRgba);

        updateHueImage();
        // 计算直方图的反投影。
        // Imgproc.calcBackProject(hueList, new MatOfInt(0), hist, prob, ranges, 255);
        Imgproc.calcBackProject(hueList, new MatOfInt(0), hist, prob, ranges, 1.0);

        // 计算两个数组的按位连接（dst = src1 & src2）计算两个数组或数组和标量的每个元素的逐位连接。
        Core.bitwise_and(prob, mask, prob, new Mat());

        // 追踪目标
        rotatedRect = Video.CamShift(prob, trackRect, new TermCriteria(TermCriteria.EPS, 10, 1));

        // 将本次最终到的目标作为下次追踪的对象
        trackRect = rotatedRect.boundingRect();

        rotatedRect.angle = -rotatedRect.angle;

        Imgproc.rectangle(prob, trackRect.tl(), trackRect.br(), new Scalar(255, 255, 0, 255), 6);

        return rotatedRect;
    }

}
