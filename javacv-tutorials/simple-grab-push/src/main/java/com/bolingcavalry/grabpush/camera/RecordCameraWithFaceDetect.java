package com.bolingcavalry.grabpush.camera;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RecordCameraWithFaceDetect extends RecordCamera {


    public static void main(String[] args) {
        new RecordCameraWithFaceDetect().action(1000);
    }
}