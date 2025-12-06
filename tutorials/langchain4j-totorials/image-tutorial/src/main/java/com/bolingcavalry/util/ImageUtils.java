package com.bolingcavalry.util;

import dev.langchain4j.data.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

/**
 * 图片处理工具类，用于处理在线图片的加载和保存
 */
public class ImageUtils {

    private static final Logger logger = LoggerFactory.getLogger(ImageUtils.class);

    /**
     * 从URL创建Image对象
     * @param imageUrl 图片的URL地址
     * @return langchain4j的Image对象
     * @throws IOException 如果图片加载失败
     */
    public static Image createImageFromUrl(String imageUrl) throws IOException {
        logger.info("从URL创建Image对象: {}", imageUrl);
        
        // 下载图片数据并转换为字节数组
        byte[] imageBytes = downloadImage(imageUrl);
        
        // 将字节数组转换为Base64编码
        String base64Data = Base64.getEncoder().encodeToString(imageBytes);
        
        logger.info("图片下载成功，原始大小: {} 字节，Base64编码后: {} 字符", 
                imageBytes.length, base64Data.length());
        
        // 使用Base64数据创建Image对象
        Image image = Image.builder()
                .base64Data(base64Data)
                .build();
        
        return image;
    }
    
    /**
     * 下载图片并返回字节数组
     * @param imageUrl 图片的URL地址
     * @return 图片的字节数组
     * @throws IOException 如果下载失败
     */
    private static byte[] downloadImage(String imageUrl) throws IOException {
        try (InputStream in = new URL(imageUrl).openStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            
            return out.toByteArray();
        }
    }

    /**
     * 从URL下载图片并保存到本地文件
     * @param imageUrl 图片的URL地址
     * @param targetPath 保存到本地的文件路径
     * @return 保存的文件路径
     * @throws IOException 如果下载或保存失败
     */
    public static Path saveImageFromUrl(String imageUrl, Path targetPath) throws IOException {
        logger.info("从URL下载图片到本地: {} -> {}", imageUrl, targetPath);
        
        // 确保目标目录存在
        Path parentDir = targetPath.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
        
        // 使用Java标准库下载图片
        try (InputStream in = new URL(imageUrl).openStream()) {
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
        
        logger.info("图片保存成功: {}", targetPath);
        return targetPath;
    }

    /**
     * 获取图片的Base64编码数据
     * @param image langchain4j的Image对象
     * @return Base64编码的字符串
     */
    public static String getImageBase64(Image image) {
        String base64Data = image.base64Data();
        if (base64Data == null) {
            logger.error("Image对象的base64Data为null");
            throw new IllegalStateException("图片数据未正确加载");
        }
        return base64Data;
    }

    /**
     * 将Base64编码的图片数据保存为文件
     * @param base64Data Base64编码的图片数据
     * @param targetPath 保存到本地的文件路径
     * @return 保存的文件路径
     * @throws IOException 如果保存失败
     */
    public static Path saveBase64Image(String base64Data, Path targetPath) throws IOException {
        logger.info("保存Base64编码的图片到: {}", targetPath);
        
        // 确保目标目录存在
        Path parentDir = targetPath.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
        
        // 解码Base64数据并保存
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);
        Files.write(targetPath, imageBytes);
        
        logger.info("Base64图片保存成功: {}, 大小: {} 字节", targetPath, imageBytes.length);
        return targetPath;
    }
}