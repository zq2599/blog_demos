package com.bolingcavalry.util;

import dev.langchain4j.data.image.Image;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ImageUtils类的测试，用于验证图片加载功能
 */
public class ImageUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(ImageUtilsTest.class);

    /**
     * 测试从URL创建Image对象是否正常工作
     */
    @Test
    public void testCreateImageFromUrl() throws IOException {
        String imageUrl = "https://scpic.chinaz.net/files/pic/pic6/pic1103.jpg";
        
        logger.info("开始测试从URL创建Image对象: {}", imageUrl);
        
        // 调用ImageUtils创建Image对象
        Image image = ImageUtils.createImageFromUrl(imageUrl);
        
        // 验证Image对象不为空
        assertNotNull(image, "Image对象不应为空");
        
        // 获取并验证Base64数据
        String base64Data = ImageUtils.getImageBase64(image);
        assertNotNull(base64Data, "Base64数据不应为空");
        assertTrue(base64Data.length() > 100, "Base64数据应有足够长度，实际长度: " + base64Data.length());
        
        logger.info("图片加载成功，Base64数据长度: {} 字符", base64Data.length());
        logger.info("Base64数据前缀: {}", base64Data.substring(0, Math.min(50, base64Data.length())) + "...");
    }
}
