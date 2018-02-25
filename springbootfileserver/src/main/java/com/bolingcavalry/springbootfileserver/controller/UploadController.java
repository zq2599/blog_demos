package com.bolingcavalry.springbootfileserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
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
 * @Description :
 * @Author : qin_zhao@kingdee.com
 * @Date : 2018-02-24 22:42
 */
@RestController
public class UploadController {
    protected static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    //生成上传文件的文件名，文件名以：uuid+"_"+文件的原始名称
    public String mkFileName(String fileName){
        return UUID.randomUUID().toString()+"_"+fileName;
    }

    public String mkFilePath(String savePath,String fileName){
        //得到文件名的hashCode的值，得到的就是filename这个字符串对象在内存中的地址
        int hashcode = fileName.hashCode();
        int dir1 = hashcode&0xf;
        int dir2 = (hashcode&0xf0)>>4;
        //构造新的保存目录
        //String dir = savePath + "\\" + dir1 + "\\" + dir2;
        String dir = savePath + File.separator + dir1 + File.separator + dir2;
        //File既可以代表文件也可以代表目录
        File file = new File(dir);
        if(!file.exists()){
            file.mkdirs();
        }
        return dir;
    }

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

        //上传文件路径
        String savePath = request.getServletContext().getRealPath("/WEB-INF/upload");

        //上传文件名
        String fileName = file.getOriginalFilename();

        logger.info("base save path [{}], original file name [{}]", savePath, fileName);

        //得到文件保存的名称
        fileName = mkFileName(fileName);

        //得到文件保存的路径
        String savePathStr = mkFilePath(savePath, fileName);

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
