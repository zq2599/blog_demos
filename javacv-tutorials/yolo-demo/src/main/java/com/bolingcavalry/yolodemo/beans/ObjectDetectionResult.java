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
    int classId;
    String className;

    float confidence;

    int x;
    int y;
    int width;
    int height;
}
