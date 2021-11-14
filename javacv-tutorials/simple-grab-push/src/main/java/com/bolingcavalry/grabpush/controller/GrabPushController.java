package com.bolingcavalry.grabpush.controller;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@Slf4j
public class GrabPushController {

    @GetMapping("/grab")
    public String grab() throws Exception {
        String url = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";

        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(url);

        grabber.start();

        Frame frame = null;

        OpenCVFrameConverter openCVFrameConverter = new OpenCVFrameConverter.ToMat();
        int i = 0;

        while ((frame=grabber.grabImage()) !=null) {
            opencv_core.Mat mat = openCVFrameConverter.convertToMat(frame);
            log.info(i++ + ". " + mat);
        }

        return "Success,  " + LocalDateTime.now();
    }
}
