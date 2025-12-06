package com.bolingcavalry.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * QwenService的集成测试类，用于测试实际的图片下载和处理功能
 */
@SpringBootTest
public class QwenServiceIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(QwenServiceIntegrationTest.class);

    @Autowired
    private QwenService qwenService;

    /**
     * 测试useImage方法是否能正常下载和处理图片
     * 注意：这个测试需要网络连接，并且会实际调用API
     */
    /*
     * @Test
     * public void testImageDownloadAndProcessing() {
     * logger.info("开始集成测试图片下载功能...");
     * try {
     * // 调用useImage方法，让AI描述图片内容
     * String prompt = "请详细描述这张图片的内容，包括主要元素、颜色和风格";
     * String result = qwenService.useImage(prompt);
     * 
     * // 记录结果
     * logger.info("useImage方法调用结果: {}", result);
     * 
     * // 验证结果不为空，表示图片可能已经成功下载并处理
     * assert result != null && !result.isEmpty() : "图片处理失败，返回空结果";
     * 
     * // 如果结果包含[from useImage]，说明方法至少成功执行了
     * assert result.contains("[from useImage]") : "方法执行不完整";
     * 
     * // 检查是否包含预期的错误信息
     * assert !result.contains("图片加载失败") : "图片加载失败";
     * assert !result.contains("处理图片时发生错误") : "图片处理发生错误";
     * 
     * // 如果模型能够处理图片，结果应该包含对图片的描述
     * logger.info("集成测试完成：方法成功执行，返回结果长度: {} 字符", result.length());
     * logger.info("图片处理流程正常，模型返回了响应");
     * } catch (Exception e) {
     * logger.error("集成测试失败：图片下载或处理出错", e);
     * throw e; // 重新抛出异常使测试失败
     * }
     * }
     */
}