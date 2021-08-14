package com.bolingcavalry.commons.utils;

import lombok.extern.slf4j.Slf4j;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @Description: 图片文件处理相关的工具方法
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2021/6/27 21:49
 */
@Slf4j
public class ImageFileUtil {

    /**
     * 调整后的文件宽度
     */
    public static final int RESIZE_WIDTH = 28;

    /**
     * 调整后的文件高度
     */
    public static final int RESIZE_HEIGHT = 28;

    /**
     * 将上传的文件存在服务器上
     * @param base 要处理的文件所在的目录
     * @param file 要处理的文件
     * @return
     */
    public static String save(String base, MultipartFile file) {

        // 检查是否为空
        if (file.isEmpty()) {
            log.error("invalid file");
            return null;
        }

        // 文件名来自原始文件
        String fileName = file.getOriginalFilename();

        // 要保存的位置
        File dest = new File(base + fileName);

        // 开始保存
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("upload fail", e);
            return null;
        }

        return fileName;
    }

    /**
     * 将图片转为28*28像素
     * @param base     处理文件的目录
     * @param fileName 待调整的文件名
     * @return
     */
    public static String resize(String base, String fileName) {

        // 新文件名是原文件名在加个随机数后缀，而且扩展名固定为png
        String resizeFileName = fileName.substring(0, fileName.lastIndexOf(".")) + "-" + UUID.randomUUID() + ".png";

        log.info("start resize, from [{}] to [{}]", fileName, resizeFileName);

        try {
            // 读原始文件
            BufferedImage bufferedImage = ImageIO.read(new File(base + fileName));

            // 缩放后的实例
            Image image = bufferedImage.getScaledInstance(RESIZE_WIDTH, RESIZE_HEIGHT, Image.SCALE_SMOOTH);

            BufferedImage resizeBufferedImage = new BufferedImage(28, 28, BufferedImage.TYPE_INT_RGB);
            Graphics graphics = resizeBufferedImage.getGraphics();

            // 绘图
            graphics.drawImage(image, 0, 0, null);
            graphics.dispose();

            // 转换后的图片写文件
            ImageIO.write(resizeBufferedImage, "png", new File(base + resizeFileName));

        } catch (Exception exception) {
            log.info("resize error from [{}] to [{}], {}", fileName, resizeFileName, exception);
            resizeFileName = null;
        }

        log.info("finish resize, from [{}] to [{}]", fileName, resizeFileName);

        return resizeFileName;
    }

    /**
     * 将RGB转为int数字
     * @param alpha
     * @param red
     * @param green
     * @param blue
     * @return
     */
    private static int colorToRGB(int alpha, int red, int green, int blue) {
        int pixel = 0;

        pixel += alpha;
        pixel = pixel << 8;

        pixel += red;
        pixel = pixel << 8;

        pixel += green;
        pixel = pixel << 8;

        pixel += blue;

        return pixel;
    }

    /**
     * 反色处理
     * @param base 处理文件的目录
     * @param src 用于处理的源文件
     * @return 反色处理后的新文件
     * @throws IOException
     */
    public static String colorRevert(String base, String src) throws IOException {
        int color, r, g, b, pixel;

        // 读原始文件
        BufferedImage srcImage = ImageIO.read(new File(base + src));

        // 修改后的文件
        BufferedImage destImage = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), srcImage.getType());

        for (int i=0; i<srcImage.getWidth(); i++) {

            for (int j=0; j<srcImage.getHeight(); j++) {
                color = srcImage.getRGB(i, j);
                r = (color >> 16) & 0xff;
                g = (color >> 8) & 0xff;
                b = color & 0xff;
                pixel = colorToRGB(255, 0xff - r, 0xff - g, 0xff - b);
                destImage.setRGB(i, j, pixel);
            }
        }

        // 反射文件的名字
        String revertFileName =  src.substring(0, src.lastIndexOf(".")) + "-revert.png";

        // 转换后的图片写文件
        ImageIO.write(destImage, "png", new File(base + revertFileName));

        return revertFileName;
    }

    /**
     * 取黑白图片的特征
     * @param base
     * @param fileName
     * @return
     * @throws Exception
     */
    public static INDArray getGrayImageFeatures(String base, String fileName) throws Exception {
        log.info("start getImageFeatures [{}]", base + fileName);

        // 和训练模型时一样的设置
        ImageRecordReader imageRecordReader = new ImageRecordReader(RESIZE_HEIGHT, RESIZE_WIDTH, 1);

        FileSplit fileSplit = new FileSplit(new File(base + fileName),
                NativeImageLoader.ALLOWED_FORMATS);

        imageRecordReader.initialize(fileSplit);

        DataSetIterator dataSetIterator = new RecordReaderDataSetIterator(imageRecordReader, 1);
        dataSetIterator.setPreProcessor(new ImagePreProcessingScaler(0, 1));

        // 取特征
        return dataSetIterator.next().getFeatures();
    }

    /**
     * 批量清理文件
     * @param base      处理文件的目录
     * @param fileNames 待清理文件集合
     */
    public static void clear(String base, String...fileNames) {
        for (String fileName : fileNames) {

            if (null==fileName) {
                continue;
            }

            File file = new File(base + fileName);

            if (file.exists()) {
                file.delete();
            }
        }
    }


}
