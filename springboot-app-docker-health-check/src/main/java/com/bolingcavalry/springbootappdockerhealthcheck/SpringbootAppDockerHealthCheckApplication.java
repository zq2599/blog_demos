package com.bolingcavalry.springbootappdockerhealthcheck;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.List;

@SpringBootApplication
@RestController
@Slf4j
public class SpringbootAppDockerHealthCheckApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootAppDockerHealthCheckApplication.class, args);
    }

    /**
     * 读取本地文本文件的内容并返回
     * @return
     */
    private String getLocalFileContent() {
        String content = null;

        try{
            InputStream is = new FileInputStream("/app/depend/abc.txt");
            List<String> lines = IOUtils.readLines(is, "UTF-8");

            if(null!=lines && lines.size()>0){
                content = lines.get(0);
            }
        } catch (FileNotFoundException e) {
            log.error("local file not found", e);
        } catch (IOException e) {
            log.error("io exception", e);
        }

        return content;
    }

    /**
     * 对外提供的http服务，读取本地的txt文件将内容返回，
     * 如果读取不到内容返回码为403
     * @return
     */
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public ResponseEntity<String> hello(){
        String localFileContent = getLocalFileContent();

        if(StringUtils.isEmpty(localFileContent)) {
            log.error("hello service error");
            return ResponseEntity.status(403).build();
        } else {
            log.info("hello service success");
            return ResponseEntity.status(200).body(localFileContent);
        }
    }

    /**
     * 该http服务返回当前应用是否正常，
     * 如果能从本地txt文件成功读取内容，当前应用就算正常，返回码为200，
     * 如果无法从本地txt文件成功读取内容，当前应用就算异常，返回码为403
     * @return
     */
    @RequestMapping(value = "/getstate", method = RequestMethod.GET)
    public ResponseEntity<String> getstate(){
        String localFileContent = getLocalFileContent();

        if(StringUtils.isEmpty(localFileContent)) {
            log.error("service is unhealthy");
            return ResponseEntity.status(403).build();
        } else {
            log.info("service is healthy");
            return ResponseEntity.status(200).build();
        }
    }

}
