package com.bolingcavalry.grabpush.camera;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class GrabImageFromCamera extends AbstractCameraApplication {

    // 图片存储路径的前缀
    protected String IMAGE_PATH_PREFIX = "E:\\temp\\202111\\28\\camera-"
            + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
            + "-";

    // 图片格式
    private final static String IMG_TYPE = "jpg";

    /**
     * 当前进程已经存储的图片数量
     */
    private int saveNums = 0;

    // 转换工具
    private Java2DFrameConverter converter = new Java2DFrameConverter();

    @Override
    protected void initOutput() throws Exception {
        // 啥也不用做
    }

    @Override
    protected void output(Frame frame) throws Exception {
        // 图片的保存位置
        String imagePath = IMAGE_PATH_PREFIX + (saveNums++) + "." + IMG_TYPE;

        // 把帧对象转为Image对象
        BufferedImage bufferedImage = converter.getBufferedImage(frame);

        // 保存图片
        ImageIO.write(bufferedImage, IMG_TYPE, new FileOutputStream(imagePath));

        log.info("保存完成：{}", imagePath);
    }

    @Override
    protected void releaseOutputResource() {
        // 啥也不用做
    }

    @Override
    protected int getInterval() {
        // 表示保存一张图片后会sleep一秒钟
        return 1000;
    }

    public static void main(String[] args) {
        // 连续十秒执行抓图操作
        new GrabImageFromCamera().action(10);
    }
}