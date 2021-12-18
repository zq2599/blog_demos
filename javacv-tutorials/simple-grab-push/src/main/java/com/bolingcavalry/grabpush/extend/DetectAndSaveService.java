package com.bolingcavalry.grabpush.extend;

import com.bolingcavalry.grabpush.Constants;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * @author willzhao
 * @version 1.0
 * @description 检测人脸并保存到硬盘的服务
 * @date 2021/12/3 8:09
 */
@Slf4j
public class DetectAndSaveService implements DetectService {

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
     * 模型文件的下载地址
     */
    private String modelFileUrl;

    /**
     * 存放人脸图片的位置
     */
    private String basePath;

    /**
     * 记录图片总数
     */
    private final AtomicInteger num = new AtomicInteger();

    /**
     * 训练的图片尺寸
     */
    Size size = new Size(Constants.RESIZE_WIDTH, Constants.RESIZE_HEIGHT);

    /**
     * 构造方法，在此指定模型文件的下载地址
     * @param modelFileUrl 人脸检测模型地址
     * @param basePath 检测出的人脸小图在硬盘上的存放地址
     */
    public DetectAndSaveService(String modelFileUrl, String basePath) {
        this.modelFileUrl = modelFileUrl;

        // 图片保存在硬盘的位置，注意文件名的固定前缀是当前的年月日时分秒
        this.basePath = basePath
                      + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                      + "-";
    }

    /**
     * 音频采样对象的初始化
     * @throws Exception
     */
    @Override
    public void init() throws Exception {
        // 下载模型文件
        URL url = new URL(modelFileUrl);

        File file = Loader.cacheResource(url);

        // 模型文件下载后的完整地址
        String classifierName = file.getAbsolutePath();

        // 根据模型文件实例化分类器
        classifier = new CascadeClassifier(classifierName);

        if (classifier == null) {
            log.error("Error loading classifier file [{}]", classifierName);
            System.exit(1);
        }
    }

    @Override
    public Frame convert(Frame frame) {
        // 由帧转为Mat
        grabbedImage = converter.convert(frame);

        // 灰度Mat，用于检测
        if (null==grayImage) {
            grayImage = DetectService.buildGrayImage(grabbedImage);
        }

        String filePath = basePath + num.incrementAndGet();

        // 进行人脸识别，根据结果做处理得到预览窗口显示的帧
        return detectAndSave(classifier, converter, frame, grabbedImage, grayImage, filePath , size);
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
     *
     * @param classifier 分类器
     * @param converter 转换工具
     * @param rawFrame 原始帧
     * @param grabbedImage 原始图片的Mat对象
     * @param grayImage 原始图片对应的灰度图片的Mat对象
     * @param basePath 图片的基本路径
     * @param size 训练时要求的图片大小
     * @return
     */
    static Frame detectAndSave(CascadeClassifier classifier,
                               OpenCVFrameConverter.ToMat converter,
                               Frame rawFrame,
                               Mat grabbedImage,
                               Mat grayImage,
                               String basePath,
                               Size size) {

        // 当前图片转为灰度图片
        cvtColor(grabbedImage, grayImage, CV_BGR2GRAY);

        // 存放检测结果的容器
        RectVector objects = new RectVector();

        // 开始检测
        classifier.detectMultiScale(grayImage, objects);

        // 检测结果总数
        long total = objects.size();

        // 如果没有检测到结果就提前返回
        if (total<1) {
            return rawFrame;
        }

        // 假设现在是一个人对着摄像头，因为此时检测的结果如果大于1，显然是检测有问题
        if (total>1) {
            return rawFrame;
        }

        Mat faceMat;

        // 如果有检测结果，就根据结果的数据构造矩形框，画在原图上
        // 前面的判断确保了此时只有一个人脸
        Rect r = objects.get(0);

        // 从完整的灰度图中取得一个矩形小图的Mat对象
        faceMat = new Mat(grayImage, r);

        // 训练时用的图片尺寸是固定的，因此这里要调整大小
        resize(faceMat, faceMat, size);

        // 图片的保存位置
        String imagePath = basePath + "." + Constants.IMG_TYPE;

        // 保存图片到硬盘
        imwrite(imagePath, faceMat);

        // 人脸的位置信息
        int x = r.x(), y = r.y(), w = r.width(), h = r.height();

        // 在人脸上画矩形
        rectangle(grabbedImage, new Point(x, y), new Point(x + w, y + h), Scalar.RED, 1, CV_AA, 0);

        // 释放检测结果资源
        objects.close();

        // 将标注过的图片转为帧，返回
        return converter.convert(grabbedImage);
    }
}
