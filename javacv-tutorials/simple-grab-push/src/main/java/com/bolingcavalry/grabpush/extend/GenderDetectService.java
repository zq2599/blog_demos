package com.bolingcavalry.grabpush.extend;

import com.bolingcavalry.grabpush.Constants;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.indexer.Indexer;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_dnn.Net;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import static org.bytedeco.opencv.global.opencv_core.NORM_MINMAX;
import static org.bytedeco.opencv.global.opencv_core.normalize;
import static org.bytedeco.opencv.global.opencv_dnn.blobFromImage;
import static org.bytedeco.opencv.global.opencv_dnn.readNetFromCaffe;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * @author willzhao
 * @version 1.0
 * @description 音频相关的服务
 * @date 2021/12/3 8:09
 */
@Slf4j
public class GenderDetectService implements DetectService {

    /**
     * 每一帧原始图片的对象
     */
    private Mat grabbedImage = null;

    /**
     * 原始图片对应的灰度图片对象
     */
    private Mat grayImage = null;

    /**
     * 分类器
     */
    private CascadeClassifier classifier;

    /**
     * 转换器
     */
    private OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

    /**
     * 人脸检测模型文件的下载地址
     */
    private String classifierModelFilePath;

    /**
     * 性别识别proto文件的下载地址
     */
    private String genderProtoFilePath;

    /**
     * 性别识别模型文件的下载地址
     */
    private String genderModelFilePath;

    /**
     * 推理性别的神经网络对象
     */
    private Net cnnNet;

    /**
     * 构造方法，在此指定proto和模型文件的下载地址
     * @param classifierModelFilePath
     * @param cnnProtoFilePath
     * @param cnnModelFilePath
     */
    public GenderDetectService(String classifierModelFilePath,
                               String cnnProtoFilePath,
                               String cnnModelFilePath) {
        this.classifierModelFilePath = classifierModelFilePath;
        this.genderProtoFilePath = cnnProtoFilePath;
        this.genderModelFilePath = cnnModelFilePath;
    }

    /**
     * 初始化操作，主要是创建推理用的神经网络
     * @throws Exception
     */
    @Override
    public void init() throws Exception {
        // 根据模型文件实例化分类器
        classifier = new CascadeClassifier(classifierModelFilePath);
        // 实例化推理性别的神经网络
        cnnNet = readNetFromCaffe(genderProtoFilePath, genderModelFilePath);
    }

    @Override
    public Frame convert(Frame frame) {
        // 由帧转为Mat
        grabbedImage = converter.convert(frame);

        // 灰度Mat，用于检测
        if (null==grayImage) {
            grayImage = DetectService.buildGrayImage(grabbedImage);
        }

        // 当前图片转为灰度图片
        cvtColor(grabbedImage, grayImage, CV_BGR2GRAY);

        // 存放检测结果的容器
        RectVector objects = new RectVector();

        // 开始检测
        classifier.detectMultiScale(grayImage, objects);

        // 检测结果总数
        long total = objects.size();

        // 如果没有检测到结果，就用原始帧返回
        if (total<1) {
            return frame;
        }

        int pos_x;
        int pos_y;

        Mat faceMat;

        //推理时的入参
        Mat inputBlob;

        // 推理结果
        Mat prob;

        // 如果有检测结果，就根据结果的数据构造矩形框，画在原图上
        for (long i = 0; i < total; i++) {
            Rect r = objects.get(i);

            // 人脸对应的Mat实例（注意：要用彩图，不能用灰度图！！！）
            faceMat = new Mat(grabbedImage, r);
            // 缩放到神经网络所需的尺寸
            resize(faceMat, faceMat, new Size(Constants.CNN_PREIDICT_IMG_WIDTH, Constants.CNN_PREIDICT_IMG_HEIGHT));
            // 归一化
            normalize(faceMat, faceMat, 0, Math.pow(2, frame.imageDepth), NORM_MINMAX, -1, null);
            // 转为推理时所需的的blob类型
            inputBlob = blobFromImage(faceMat);
            // 为神经网络设置入参
            cnnNet.setInput(inputBlob, "data", 1.0, null);      //set the network input
            // 推理
            prob = cnnNet.forward("prob");

            // 根据推理结果得到在人脸上标注的内容
            String lable = getDescriptionFromPredictResult(prob);

            // 人脸标注的横坐标
            pos_x = Math.max(r.tl().x()-10, 0);
            // 人脸标注的纵坐标
            pos_y = Math.max(r.tl().y()-10, 0);

            // 给人脸做标注，标注性别
            putText(grabbedImage, lable, new Point(pos_x, pos_y), FONT_HERSHEY_PLAIN, 1.5, new Scalar(0,255,0,2.0));

            // 给人脸加边框时的边框位置
            int x = r.x(), y = r.y(), w = r.width(), h = r.height();
            // 给人脸加边框
            rectangle(grabbedImage, new Point(x, y), new Point(x + w, y + h), Scalar.RED, 1, CV_AA, 0);
        }

        // 释放检测结果资源
        objects.close();

        // 将标注过的图片转为帧，返回
        return converter.convert(grabbedImage);
    }

    /**
     * 程序结束前，释放人脸识别的资源
     */
    @Override
    public void releaseOutputResource() {
        if (null!=grabbedImage) {
            grabbedImage.release();
        }

        if (null!=grayImage) {
            grayImage.release();
        }

        if (null!=classifier) {
            classifier.close();
        }

        if (null!= cnnNet) {
            cnnNet.close();
        }
    }

    /**
     * 根据推理结果得到在头像上要标注的内容
     * @param prob
     * @return
     */
    protected String getDescriptionFromPredictResult(Mat prob) {
        Indexer indexer = prob.createIndexer();

        // 比较两种性别的概率，概率大的作为当前头像的性别
        return indexer.getDouble(0,0) > indexer.getDouble(0,1)
               ? "male"
               : "female";
    }
}
