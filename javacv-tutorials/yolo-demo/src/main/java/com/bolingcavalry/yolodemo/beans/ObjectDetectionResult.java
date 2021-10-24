package com.bolingcavalry.yolodemo.beans;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2021/10/17 5:10 下午
 * @description 存数据的bean
 */
@Data
@AllArgsConstructor
public class ObjectDetectionResult {
    // 类别索引
    int classId;
    // 类别名称
    String className;
    // 置信度
    float confidence;
    // 物体在照片中的横坐标
    int x;
    // 物体在照片中的纵坐标
    int y;
    // 物体宽度
    int width;
    // 物体高度
    int height;
}
