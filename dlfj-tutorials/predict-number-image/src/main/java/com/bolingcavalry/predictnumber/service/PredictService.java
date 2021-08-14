package com.bolingcavalry.predictnumber.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Description: 数字识别接口
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2021/6/27 15:28
 */
public interface PredictService {

    /**
     * 取得上传的图片，做转换后识别成数字
     * @param file 上传的文件
     * @param isNeedRevert 是否要做反色处理
     * @return
     */
    int predict(MultipartFile file, boolean isNeedRevert) throws Exception ;
}
