package com.bolingcavalry.grabpush.controller;

//import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
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

    public static void main(String[] args) throws FileNotFoundException {
        File tempFile = new File("/Users/zhaoqin/temp/202111/14/", "100410A000.mp4");
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(new FileInputStream(tempFile));
        System.out.println(grabber);
    }


}
