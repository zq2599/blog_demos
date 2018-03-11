package com.bolingcavalry.k8spvdemo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

/**
 * @Description : 上传文件的服务
 * @Author : zq2599@gmail.com
 * @Date : 2018-03-11 16:51
 */
@RestController
public class UploadController {
    protected static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    public void responseAndClose(HttpServletResponse response, String message){
        response.reset();
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
        }catch (IOException exception){
            logger.error("get writer error, ", exception);
        }

        if(null!=out){
            if (!StringUtils.isEmpty(message)) {
                out.print(message);
            }

            out.close();
        }
    }

    //上传文件会自动绑定到MultipartFile中
    @RequestMapping(value="/upload",method= RequestMethod.POST)
    public void upload(HttpServletRequest request,
                       HttpServletResponse response,
                       @RequestParam("comment") String comment,
                       @RequestParam("file") MultipartFile file) throws Exception {

        logger.info("start upload, comment [{}]", comment);

        if(null==file || file.isEmpty()){
            logger.error("file item is empty!");
            responseAndClose(response, "文件数据为空");
            return;
        }

        //上传文件名
        String fileName = file.getOriginalFilename();

        //得到文件保存的路径
        String savePathStr = "/usr/local/uploadfiles";

        logger.info("real save path [{}], real file name [{}]", savePathStr, fileName);

        File filepath = new File(savePathStr, fileName);

        //确保路径存在
        if(!filepath.getParentFile().exists()){
            logger.info("real save path is not exists, create now");
            filepath.getParentFile().mkdirs();
        }

        String fullSavePath = savePathStr + File.separator + fileName;

        //存本地
        file.transferTo(new File(fullSavePath));

        logger.info("save file success [{}]", fullSavePath);

        responseAndClose(response, "SpringBoot环境下，上传文件成功");
    }
}
