package com.bolingcavalry.grabpush.extend;

import com.bolingcavalry.grabpush.Constants;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.indexer.Indexer;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_dnn.Net;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

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
    private String classifierModelFileUrl;

    /**
     * 性别识别proto文件的下载地址
     */
    private String genderProtoFileUrl;

    /**
     * 性别识别模型文件的下载地址
     */
    private String genderModelFileUrl;

    /**
     * 年龄识别proto文件的下载地址
     */
    private String ageProtoFileUrl;

    /**
     * 年龄识别模型文件的下载地址
     */
    private String ageModelFileUrl;

    /**
     * 推理性别的神经网络对象
     */
    private Net genderNet;

    /**
     * 推理年龄的神经网络对象
     */
    private Net ageNet;

    /**
     * 构造方法，在此指定proto和模型文件的下载地址
     * @param classifierModelFileUrl
     * @param genderProtoFileUrl
     * @param genderModelFileUrl
     * @param ageProtoFileUrl
     * @param ageModelFileUrl
     */
    public GenderDetectService(String classifierModelFileUrl,
                               String genderProtoFileUrl,
                               String genderModelFileUrl,
                               String ageProtoFileUrl,
                               String ageModelFileUrl) {
        this.classifierModelFileUrl = classifierModelFileUrl;
        this.genderProtoFileUrl = genderProtoFileUrl;
        this.genderModelFileUrl = genderModelFileUrl;
        this.ageProtoFileUrl = ageProtoFileUrl;
        this.ageModelFileUrl = ageModelFileUrl;
    }

    /**
     * 初始化操作，主要是创建推理用的神经网络
     * @throws Exception
     */
    @Override
    public void init() throws Exception {
        // 根据模型文件实例化分类器
        classifier = new CascadeClassifier(download(classifierModelFileUrl));
        // 实例化推理性别的神经网络
        genderNet = readNetFromCaffe(download(genderProtoFileUrl), download(genderModelFileUrl));
        // 实例化推理年龄的神经网络
        ageNet = readNetFromCaffe(download(ageProtoFileUrl), download(ageModelFileUrl));
    }

    @Override
    public Frame convert(Frame frame) {
        // 由帧转为Mat
        grabbedImage = converter.convert(frame);

        // 灰度Mat，用于检测
        if (null==grayImage) {
            grayImage = DetectService.buildGrayImage(grabbedImage);
        }

        // 进行人脸识别，根据结果做处理得到预览窗口显示的帧
        return detectAndPredictGenderAge(classifier, converter, frame, grabbedImage, grayImage, genderNet, ageNet);
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

        if (null==classifier) {
            classifier.close();
        }
    }

    /**
     * 远程下载文件到本地，再返回本地文件路径
     * @param remotePath
     * @return 下载到本地后的本地文件地址
     */
    static String download(String remotePath) {
        log.info("正在下载 : {}", remotePath);
        File file = null;

        try {
            file = Loader.cacheResource(new URL(remotePath));
            log.info("下载完成");
        } catch (MalformedURLException malformedURLException) {
            malformedURLException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return null==file ? null : file.getAbsolutePath();
    }

    /**
     * 检测图片，将检测结果用矩形标注在原始图片上
     * @param classifier 分类器
     * @param converter Frame和mat的转换器
     * @param rawFrame 原始视频帧
     * @param grabbedImage 原始视频帧对应的mat
     * @param grayImage 存放灰度图片的mat
     * @param genderNet 预测性别的神经网络
     * @param ageNet 预测性别的神经网络
     * @return 标注了识别结果的视频帧
     */
    static Frame detectAndPredictGenderAge(CascadeClassifier classifier,
                                    OpenCVFrameConverter.ToMat converter,
                                    Frame rawFrame,
                                    Mat grabbedImage,
                                    Mat grayImage,
                                    Net genderNet,
                                    Net ageNet) {

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
            return rawFrame;
        }

        int pos_x;
        int pos_y;
        String content;

        Mat faceMat;

        //推理时的入参
        Mat inputBlob;

        // 推理结果
        Mat prob;

        // 索引
        Indexer indexer;

        boolean male = true;

        // 如果有检测结果，就根据结果的数据构造矩形框，画在原图上
        for (long i = 0; i < total; i++) {
            Rect r = objects.get(i);

            // 人脸对应的Mat实例
            faceMat = new Mat(grayImage, r);
            // 缩放到神经网络所需的尺寸
            resize(faceMat, faceMat, new Size(Constants.CNN_PREIDICT_IMG_WIDTH, Constants.CNN_PREIDICT_IMG_HEIGHT));
            // 归一化
            normalize(faceMat, faceMat, 0, Math.pow(2, rawFrame.imageDepth), NORM_MINMAX, -1, null);
            // 转为推理时所需的的blob类型
            inputBlob = blobFromImage(faceMat);
            // 为神经网络设置入参
            genderNet.setInput(inputBlob, "data", 1.0, null);      //set the network input
            // 推理
            prob = genderNet.forward("prob");

            indexer = prob.createIndexer();
            // 比较两种性别的概率，概率大的作为当前头像的性别
            male = indexer.getDouble(0,0) > indexer.getDouble(0,1);


            content = String.format("%s", male ?  "male" : "female");




//            predictRlt = recognizeService.predict(new Mat(grayImage, r));

            // 如果返回为空，表示出现过异常，就执行下一个
//            if (null==predictRlt) {
//                System.out.println("return null");
//                continue;
//            }

            // 分类的编号（训练时只有1和2，这里只有有三个值，1和2与训练的分类一致，还有个-1表示没有匹配上）
//            lable = predictRlt.getLable();
            // 与模型中的分类的距离，值越小表示相似度越高
//            confidence = predictRlt.getConfidence();

            // 得到分类编号后，从map中取得名字，用来显示
//            if (kindNameMap.containsKey(predictRlt.getLable())) {
//                content = String.format("%s, confidence : %.4f", kindNameMap.get(lable), confidence);
//            } else {
//                // 取不到名字的时候，就显示unknown
//                content = "unknown(" + predictRlt.getLable() + ")";
//                System.out.println(content);
//            }

            int x = r.x(), y = r.y(), w = r.width(), h = r.height();
            rectangle(grabbedImage, new Point(x, y), new Point(x + w, y + h), Scalar.RED, 1, CV_AA, 0);

            pos_x = Math.max(r.tl().x()-10, 0);
            pos_y = Math.max(r.tl().y()-10, 0);

            putText(grabbedImage, content, new Point(pos_x, pos_y), FONT_HERSHEY_PLAIN, 1.5, new Scalar(0,255,0,2.0));
        }

        // 释放检测结果资源
        objects.close();

        // 将标注过的图片转为帧，返回
        return converter.convert(grabbedImage);
    }
}
