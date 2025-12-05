package com.bolingcavalry.config;

import lombok.Data;

/**
 * 图像编辑模型参数配置类
 */
@Data
public class ImageEditModelParam {
    private String modelName;
    private String baseUrl;
    private String apiKey;
}
