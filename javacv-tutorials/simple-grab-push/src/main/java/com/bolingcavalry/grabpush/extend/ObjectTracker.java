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

    /**
     * 上一个矩形和当前矩形的差距达到多少的时候，才算跟丢，您可以自行调整
     */
    private static final double LOST_GATE = 0.8d;

    // [0.0, 256.0]表示直方图能表示像素值从0.0到256的像素
    private static final MatOfFloat RANGES = new MatOfFloat(0f, 256f);

    private Mat mask;

    // 保存用来追踪的每一帧的反向投影图
    private Mat prob;

    // 保存最近一次确认的头像的位置，每当新的一帧到来时，都从这个位置开始追踪（也就是反向投影图做CamShift计算的起始位置）
    private Rect trackRect;

    // 直方图
    private Mat hist;


    public ObjectTracker(Mat rgba) {
        hist = new Mat();
        trackRect = new Rect();
        mask = new Mat(rgba.size(), CvType.CV_8UC1);
        prob = new Mat(rgba.size(), CvType.CV_8UC1);
    }

    /**
     * 将摄像头传来的图片提取出hue通道，放入hueList中
     * 将摄像头传来的RGB颜色空间的图片转为HSV颜色空间，
     * 然后检查HSV三个通道的值是否在指定范围内，mask中记录了检查结果
     * 再将hsv中的hue提取出来
     * @param rgba
     */
    private List<Mat> rgba2Hue(Mat rgba) {
        // 实例化Mat，显然，hsv是三通道，hue是hsv三通道其中的一个，所以hue是一通道
        Mat hsv = new Mat(rgba.size(), CvType.CV_8UC3);
        Mat hue = new Mat(rgba.size(), CvType.CV_8UC1);

        // 1. 先转换
        // 转换颜色空间，RGB到HSV
        Imgproc.cvtColor(rgba, hsv, Imgproc.COLOR_RGB2HSV);

        int vMin = 65, vMax = 256, sMin = 55;
        //inRange函数的功能是检查输入数组每个元素大小是否在2个给定数值之间，可以有多通道,mask保存0通道的最小值，也就是h分量
        //这里利用了hsv的3个通道，比较h,0~180,s,smin~256,v,min(vmin,vmax),max(vmin,vmax)。如果3个通道都在对应的范围内，
        //则mask对应的那个点的值全为1(0xff)，否则为0(0x00).
        Core.inRange(
                hsv,
                new Scalar(0, sMin, Math.min(vMin, vMax)),
                new Scalar(180, 256, Math.max(vMin, vMax)),
                mask
        );

        // 2. 再提取
        // 把hsv的数据放入hsvList中，用于稍后提取出其中的hue
        List<Mat> hsvList = new Vector<>();
        hsvList.add(hsv);

        // 准备好hueList，用于接收通道
        // hue初始化为与hsv大小深度一样的矩阵，色调的度量是用角度表示的，红绿蓝之间相差120度，反色相差180度
        hue.create(hsv.size(), hsv.depth());

        List<Mat> hueList = new Vector<>();
        hueList.add(hue);

        // 描述如何提取：从目标的0位置提取到目的地的0位置
        MatOfInt from_to = new MatOfInt(0, 0);

        // 提取操作：将hsv第一个通道(也就是色调)的数复制到hue中，0索引数组
        Core.mixChannels(hsvList, hueList, from_to);

        return hueList;
    }

    /**
     * 当外部调用方确定了人脸在图片中的位置后，就可以调用createTrackedObject开始跟踪，
     * 该方法中会先生成人脸的hue的直方图，用于给后续帧生成反向投影
     * @param mRgba
     * @param region
     */
    public void createTrackedObject(Mat mRgba, Rect region) {
        hist.release();

        //将摄像头的视频帧转化成hsv，然后再提取出其中的hue通道
        List<Mat> hueList = rgba2Hue(mRgba);

        // 人脸区域的mask
        Mat tempMask = mask.submat(region);

        // histSize表示这个直方图分成多少份（即多少个直方柱），就是 bin的个数
        MatOfInt histSize = new MatOfInt(25);
        // 只要头像区域的数据
        List<Mat> images = Collections.singletonList(hueList.get(0).submat(region));
        // 计算头像的hue直方图，结果在hist中
        Imgproc.calcHist(images, new MatOfInt(0), tempMask, hist, histSize, RANGES);

        // 将hist矩阵进行数组范围归一化，都归一化到0~255
        Core.normalize(hist, hist, 0, 255, Core.NORM_MINMAX);

        // 这个trackRect记录了人脸最后一次出现的位置，后面新的帧到来时，就从trackRect位置开始做CamShift计算
        trackRect = region;
    }

    /**
     * 在开始跟踪后，每当摄像头新的一帧到来时，外部就会调用objectTracking，将新的帧传入，
     * 此时，会用前面准备好的人脸hue直方图，将新的帧计算出反向投影图，
     * 再在反向投影图上执行CamShift计算，找到密度最大处，即人脸在新的帧上的位置，
     * 将这个位置作为返回值，返回
     * @param mRgba 新的一帧
     * @return 人脸在新的一帧上的位置
     */
    public Rect objectTracking(Mat mRgba) {
        // 新的图片，提取hue
        List<Mat> hueList;
        try {
           // 实测此处可能抛出异常，要注意捕获，避免程序退出
            hueList = rgba2Hue(mRgba);
        } catch (CvException cvException) {
            log.error("cvtColor exception", cvException);
            trackRect = null;
            return null;
        }

        // 用头像直方图在新图片的hue通道数据中计算反向投影。
        Imgproc.calcBackProject(hueList, new MatOfInt(0), hist, prob, RANGES, 1.0);
        // 计算两个数组的按位连接（dst = src1 & src2）计算两个数组或数组和标量的每个元素的逐位连接。
        Core.bitwise_and(prob, mask, prob, new Mat());

        // 在反向投影上进行CamShift计算，返回值就是密度最大处，即追踪结果
        RotatedRect rotatedRect = Video.CamShift(prob, trackRect, new TermCriteria(TermCriteria.EPS, 10, 1));

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
