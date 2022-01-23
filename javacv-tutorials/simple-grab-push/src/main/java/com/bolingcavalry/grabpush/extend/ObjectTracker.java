package com.bolingcavalry.grabpush.extend;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
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
@Slf4j
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
//        hist.release();
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

    public Rect objectTracking(Mat mRgba) {
        try {
           // 实测此处可能抛出异常，要注意捕获，避免程序退出
           rgba2Hsv(mRgba);
        } catch (CvException cvException) {
            log.error("cvtColor exception", cvException);
            trackRect = null;
            return null;
        }

        updateHueImage();
        // 计算直方图的反投影。
        Imgproc.calcBackProject(hueList, new MatOfInt(0), hist, prob, ranges, 1.0);

        // 计算两个数组的按位连接（dst = src1 & src2）计算两个数组或数组和标量的每个元素的逐位连接。
        Core.bitwise_and(prob, mask, prob, new Mat());

        // 追踪目标
        rotatedRect = Video.CamShift(prob, trackRect, new TermCriteria(TermCriteria.EPS, 10, 1));

        // 转为Rect对象
        Rect camShiftRect = rotatedRect.boundingRect();

        // 比较追踪前和追踪后的数据，如果出现太大偏差，就认为追踪失败
        if (lostTrace(trackRect, camShiftRect)) {
            log.info("lost trace!");
            trackRect = null;
            return null;
        }

        // 将本次最终到的目标作为下次追踪的对象
        trackRect = camShiftRect;

        return camShiftRect;
    }

    private static final double LOST_GATE = 0.8d;

    /**
     * 变化率的绝对值
     * @param last 变化前
     * @param current 变化后
     * @return
     */
    private static double changeRate(int last, int current) {
        return Math.abs((double)(current-last)/(double) last);
    }

    /**
     * 本次和上一次宽度或者高度的变化率，一旦超过阈值就认为跟踪失败
     * @param lastRect
     * @param currentRect
     * @return
     */
    private static boolean lostTrace(Rect lastRect, Rect currentRect) {
        // 0不能做除数，如果发现0就认跟丢了
        if (lastRect.width<1 || lastRect.height<1) {
            return true;
        }

        double widthChangeRate = changeRate(lastRect.width, currentRect.width);

        if (widthChangeRate>LOST_GATE) {
            log.info("1. lost trace, old [{}], new [{}], rate [{}]", lastRect.width, currentRect.width, widthChangeRate);
            return true;
        }

        double heightChangeRate = changeRate(lastRect.height, currentRect.height);

        if (heightChangeRate>LOST_GATE) {
            log.info("2. lost trace, old [{}], new [{}], rate [{}]", lastRect.height, currentRect.height, heightChangeRate);
            return true;
        }

        return false;
    }

}
