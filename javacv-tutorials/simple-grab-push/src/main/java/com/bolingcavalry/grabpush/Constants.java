package com.bolingcavalry.grabpush;

public interface Constants {
    /**
     * 调整后的文件宽度
     */
    int RESIZE_WIDTH = 164;
    /**
     * 调整后的文件高度
     */
    int RESIZE_HEIGHT = 164;

    /**
     * 超过这个置信度就明显有问题了
     */
    double MAX_CONFIDENCE = 50d;

    /**
     * 卷积神经网络推理使用的图片宽度
     */
    int CNN_PREIDICT_IMG_WIDTH = 256;

    /**
     * 卷积神经网络推理使用的图片高度
     */
    int CNN_PREIDICT_IMG_HEIGHT = 256;


    /**
     * 卷积神经网络推理使用的图片宽度
     */
    int CAFFE_MASK_MODEL_IMG_WIDTH = 160;

    /**
     * 卷积神经网络推理使用的图片高度
     */
    int CAFFE_MASK_MODEL_IMG_HEIGHT = 160;

    String IMG_TYPE = "jpg";
}
