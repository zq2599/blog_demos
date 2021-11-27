package com.bolingcavalry.grabpush.camera;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PreviewCameraWithFaceDetect extends PreviewCamera {



    public static void main(String[] args) {
        new PreviewCameraWithFaceDetect().action(1000);
    }
}