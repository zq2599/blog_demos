package com.bolingcavalry.grabpush.extend;

import lombok.Data;

/**
 * @author willzhao
 * @version 1.0
 * @description 推理结果
 * @date 2021/12/14 8:18
 */
@Data
public class PredictRlt {
    private int lable;
    private double confidence;
}
