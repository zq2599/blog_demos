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

    String IMG_TYPE = "jpg";
}
