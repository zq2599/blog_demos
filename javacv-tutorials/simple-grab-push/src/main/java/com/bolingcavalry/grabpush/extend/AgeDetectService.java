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

    /**
     * 设置训练模型时划分的年龄段，所以推理结果也是这样的年龄段
     */
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

        // 把prob理解为一个数组，
        // 第一个元素是"0-2"的置信度
        // 第二个元素是"4-6"的置信度
        // 第三个元素是"8-13"的置信度
        // 第四个元素是"15-20"的置信度
        // ...
        // 第八个元素是"60-"的置信度
        // minMaxLoc方法帮忙我们找出了置信度最高的元素，max是元素位置，pointer是这个元素的置信度
        minMaxLoc(prob, null, pointer, null, max, null);

        // 如果置信度太低，那就是"难以置信"，就返回空字符串
        if (pointer.get()<0.5d) {
            return "";
        } else {
            // 如果置信度可信，就返回该元素对应的年龄范围
            return AGES[max.x()];
        }
    }
}
