package com.bolingcavalry.facedetect.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.util.UUID;

import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;

@Controller
@Slf4j
public class UploadController {

    static {
        // 加载 动态链接库
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private final ResourceLoader resourceLoader;

    @Autowired
    public UploadController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Value("${web.upload-path}")
    private String uploadPath;

    @Value("${opencv.model-path}")
    private String modelPath;

    /**
     * 跳转到文件上传页面
     * @return
     */
    @RequestMapping("index")
    public String toUpload(){
        return "index";
    }

    /**
     * 上次文件到指定目录
     * @param file 文件
     * @param path 文件存放路径
     * @param fileName 源文件名
     * @return
     */
    private static boolean upload(MultipartFile file, String path, String fileName){
        //使用原文件名
        String realPath = path + "/" + fileName;

        File dest = new File(realPath);

        //判断文件父目录是否存在
        if(!dest.getParentFile().exists()){
            dest.getParentFile().mkdir();
        }

        try {
            //保存文件
            file.transferTo(dest);
            return true;
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    /**
     *
     * @param file 要上传的文件
     * @return
     */
    @RequestMapping("fileUpload")
    public String upload(@RequestParam("fileName") MultipartFile file, @RequestParam("minneighbors") int minneighbors, Map<String, Object> map){
        log.info("file [{}], size [{}], minneighbors [{}]", file.getOriginalFilename(), file.getSize(), minneighbors);

        String originalFileName = file.getOriginalFilename();
        if (!upload(file, uploadPath, originalFileName)){
            map.put("msg", "上传失败！");
            return "forward:/index";
        }

        String realPath = uploadPath + "/" + originalFileName;

        Mat srcImg = Imgcodecs.imread(realPath);

        // 目标灰色图像
        Mat dstGrayImg = new Mat();
        // 转换灰色
        Imgproc.cvtColor(srcImg, dstGrayImg, Imgproc.COLOR_BGR2GRAY);
        // OpenCv人脸识别分类器
        CascadeClassifier classifier = new CascadeClassifier(modelPath);
        // 用来存放人脸矩形
        MatOfRect faceRect = new MatOfRect();

        // 特征检测点的最小尺寸
        Size minSize = new Size(32, 32);
        // 图像缩放比例,可以理解为相机的X倍镜
        double scaleFactor = 1.2;
        // 执行人脸检测
        classifier.detectMultiScale(dstGrayImg, faceRect, scaleFactor, minneighbors, CV_HAAR_DO_CANNY_PRUNING, minSize);
        //遍历矩形,画到原图上面
        // 定义绘制颜色
        Scalar color = new Scalar(0, 0, 255);

        Rect[] rects = faceRect.toArray();

        // 没检测到
        if (null==rects || rects.length<1) {
            // 显示图片
            map.put("msg", "未检测到人脸");
            // 文件名
            map.put("fileName", originalFileName);

            return "forward:/index";
        }

        // 逐个处理
        for(Rect rect: rects) {
            int x = rect.x;
            int y = rect.y;
            int w = rect.width;
            int h = rect.height;
            // 单独框出每一张人脸
            Imgproc.rectangle(srcImg, new Point(x, y), new Point(x + w, y + w), color, 2);
        }

        // 添加人脸框之后的图片的名字
        String newFileName = UUID.randomUUID().toString() + ".png";

        // 保存
        Imgcodecs.imwrite(uploadPath + "/" + newFileName, srcImg);

        // 显示图片
        map.put("msg", "一共检测到" + rects.length + "个人脸");
        // 文件名
        map.put("fileName", newFileName);

        return "forward:/index";
    }

    /**
     * 显示单张图片
     * @return
     */
    @RequestMapping("show")
    public ResponseEntity showPhotos(String fileName){
        if (null==fileName) {
            return ResponseEntity.notFound().build();
        }

        try {
            // 由于是读取本机的文件，file是一定要加上的， path是在application配置文件中的路径
            return ResponseEntity.ok(resourceLoader.getResource("file:" + uploadPath + "/" + fileName));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
