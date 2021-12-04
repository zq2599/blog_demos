package com.bolingcavalry.grabpush.camera;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import java.io.File;
import java.net.URL;
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.indexer.*;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.*;
import org.bytedeco.opencv.opencv_calib3d.*;
import org.bytedeco.opencv.opencv_objdetect.*;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_calib3d.*;
import static org.bytedeco.opencv.global.opencv_objdetect.*;

@Slf4j
public class PreviewCameraFaceDetect extends AbstractCameraApplication {

    /**
     * 本机窗口
     */
    protected CanvasFrame previewCanvas;

    private OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

    String classifierName = null;
    private Mat grabbedImage;
    private Mat grayImage;
    private CascadeClassifier classifier;

    @Override
    protected void initOutput() throws IOException {
        previewCanvas = new CanvasFrame("摄像头预览", CanvasFrame.getDefaultGamma() / grabber.getGamma());
        previewCanvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        previewCanvas.setAlwaysOnTop(true);

        if (null==classifierName) {
            URL url = new URL("https://raw.github.com/opencv/opencv/master/data/haarcascades/haarcascade_frontalface_alt.xml");
            File file = Loader.cacheResource(url);
            classifierName = file.getAbsolutePath();
        }

        // We can "cast" Pointer objects by instantiating a new object of the desired class.
        classifier = new CascadeClassifier(classifierName);
        if (classifier == null) {
            log.error("Error loading classifier file [{}]", classifierName);
            System.exit(1);
        }

        grabbedImage = converter.convert(grabber.grab());
        int height = grabbedImage.rows();
        int width = grabbedImage.cols();

        grayImage = new Mat(height, width, CV_8UC1);
    }

    @Override
    protected void output(Frame frame) {
        grabbedImage = converter.convert(frame);

        cvtColor(grabbedImage, grayImage, CV_BGR2GRAY);

        RectVector faces = new RectVector();

        classifier.detectMultiScale(grayImage, faces);

        long total = faces.size();

        if (total<1) {
            previewCanvas.showImage(frame);
            return;
        }

        for (long i = 0; i < total; i++) {
            Rect r = faces.get(i);
            int x = r.x(), y = r.y(), w = r.width(), h = r.height();
            rectangle(grabbedImage, new Point(x, y), new Point(x + w, y + h), Scalar.RED, 1, CV_AA, 0);
        }

        // 预览窗口上显示当前帧
        previewCanvas.showImage(converter.convert(grabbedImage));
    }

    @Override
    protected void releaseOutputResource() {
        if (null!= previewCanvas) {
            previewCanvas.dispose();
        }
    }

    public static void main(String[] args) {
        new PreviewCameraFaceDetect().action(1000);
    }
}