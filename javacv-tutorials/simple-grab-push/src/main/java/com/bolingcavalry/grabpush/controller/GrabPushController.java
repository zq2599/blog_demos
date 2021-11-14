package com.bolingcavalry.grabpush.controller;

//import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;

//@RestController
//@Slf4j
public class GrabPushController {

//    @GetMapping("/grab")
//    public String grab() throws Exception {
////        String url = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";
//        String url = "/Users/zhaoqin/temp/202111/14/100410A000.mp4";
//
//        File tempFile = new File("/Users/zhaoqin/temp/202111/14/", "100410A000.mp4");
//        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(new FileInputStream(tempFile));
//
//
//        return "Success,  " + LocalDateTime.now();
//    }

    public static void main(String[] args) throws Exception {
//        File tempFile = new File("/Users/zhaoqin/temp/202111/14/", "100410A000.mp4");
//        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(new FileInputStream(tempFile));

        String url = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(url);
        grabber.start();
        Frame frame;

        Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter();

        String imageMat = "jpg";

        for (int i = 0 ; i < 10 ; i++) {
            frame = grabber.grabImage();

            BufferedImage bufferedImage = java2DFrameConverter.getBufferedImage(frame);

            String fileName = "/Users/zhaoqin/temp/202111/15/images/video-frame-" + i + "." + imageMat;
            File output = new File(fileName);

            ImageIO.write(bufferedImage, imageMat, output);
            System.out.println("save [" + fileName + "]");
        }

        grabber.stop();
    }


}
