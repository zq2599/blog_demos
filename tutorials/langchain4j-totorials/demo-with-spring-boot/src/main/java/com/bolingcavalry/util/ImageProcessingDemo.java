package com.bolingcavalry.util;

import dev.langchain4j.data.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 图片处理演示类，展示如何使用ImageUtils类处理在线图片
 */
public class ImageProcessingDemo {

    private static final Logger logger = LoggerFactory.getLogger(ImageProcessingDemo.class);

    /**
     * 演示完整的图片处理流程
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            // 示例图片URL
            String imageUrl = "https://scpic.chinaz.net/files/pic/pic6/pic1103.jpg";
            
            // 本地保存路径
            String saveDir = "./downloaded_images";
            String directDownloadFileName = "direct_download.jpg";
            String base64DownloadFileName = "from_base64.jpg";
            
            // 1. 从URL创建Image对象
            logger.info("=== 步骤1: 从URL创建Image对象 ===");
            Image image = ImageUtils.createImageFromUrl(imageUrl);
            logger.info("Image对象创建成功");
            
            // 2. 获取图片的Base64编码数据
            logger.info("\n=== 步骤2: 获取图片的Base64编码数据 ===");
            String base64Data = ImageUtils.getImageBase64(image);
            logger.info("Base64数据长度: {} 字符", base64Data.length());
            logger.info("Base64数据前缀: {}", base64Data.substring(0, Math.min(50, base64Data.length())) + "...");
            
            // 3. 直接从URL下载图片到本地
            logger.info("\n=== 步骤3: 直接从URL下载图片到本地 ===");
            Path directDownloadPath = Paths.get(saveDir, directDownloadFileName);
            Path savedPath = ImageUtils.saveImageFromUrl(imageUrl, directDownloadPath);
            logger.info("图片已保存到: {}", savedPath.toAbsolutePath());
            
            // 4. 从Base64数据保存图片到本地
            logger.info("\n=== 步骤4: 从Base64数据保存图片到本地 ===");
            Path base64SavePath = Paths.get(saveDir, base64DownloadFileName);
            Path base64SavedPath = ImageUtils.saveBase64Image(base64Data, base64SavePath);
            logger.info("Base64图片已保存到: {}", base64SavedPath.toAbsolutePath());
            
            logger.info("\n=== 图片处理流程演示完成 ===");
            logger.info("直接下载的图片: {}", directDownloadPath.toAbsolutePath());
            logger.info("从Base64保存的图片: {}", base64SavePath.toAbsolutePath());
            
        } catch (Exception e) {
            logger.error("图片处理过程中出现错误", e);
        }
    }
    
    /**
     * 在Spring Boot应用中使用的示例方法
     * 可以集成到服务层使用
     */
    public static void processImageInSpringBoot(String imageUrl, String savePath) {
        try {
            // 1. 创建Image对象
            Image image = ImageUtils.createImageFromUrl(imageUrl);
            
            // 2. 获取Base64数据
            String base64Data = ImageUtils.getImageBase64(image);
            
            // 3. 保存图片到指定路径
            Path path = Paths.get(savePath);
            ImageUtils.saveBase64Image(base64Data, path);
            
            logger.info("Spring Boot应用中的图片处理完成: {}", path.toAbsolutePath());
        } catch (Exception e) {
            logger.error("Spring Boot应用中的图片处理失败", e);
            throw new RuntimeException("图片处理失败", e);
        }
    }
}