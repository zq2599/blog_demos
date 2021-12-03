package com.bolingcavalry.grabpush.plugin;

import com.bolingcavalry.grabpush.camera.AbstractCameraApplication;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2021/11/28 19:26
 * @description 插件定义，用于扩展输出能力
 */
public interface OutputPlugin {
    void doBeforeStart(FrameRecorder recorder, AbstractCameraApplication cameraApplication) throws Exception;


    void output(Frame frame);


    void releaseOutputResource();

    void doAfterStart();
}
