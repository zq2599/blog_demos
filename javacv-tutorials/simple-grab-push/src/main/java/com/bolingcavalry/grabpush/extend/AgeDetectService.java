package com.bolingcavalry.grabpush.extend;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;

import static org.bytedeco.opencv.global.opencv_core.minMaxLoc;

/**
 * @author willzhao
 * @version 1.0
 * @description 检测年龄的服务
 * @date 2021/12/3 8:09
 */
@Slf4j
public class AgeDetectService extends GenderDetectService {

    private static final String[] AGES = new String[]{"0-2", "4-6", "8-13", "15-20", "25-32", "38-43", "48-53", "60-"};

    /**
     * 构造方法，在此指定proto和模型文件的下载地址
     *
     * @param classifierModelFilePath
     * @param cnnProtoFilePath
     * @param cnnModelFilePath
     */
    public AgeDetectService(String classifierModelFilePath, String cnnProtoFilePath, String cnnModelFilePath) {
        super(classifierModelFilePath, cnnProtoFilePath, cnnModelFilePath);
    }

    @Override
    protected String getDescriptionFromPredictResult(Mat prob) {
        DoublePointer pointer = new DoublePointer(new double[1]);
        Point max = new Point();
        minMaxLoc(prob, null, pointer, null, max, null);
        return AGES[max.x()];
    }
}
