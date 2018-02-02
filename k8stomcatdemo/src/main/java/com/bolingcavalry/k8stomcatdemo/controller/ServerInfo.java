package com.bolingcavalry.k8stomcatdemo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description : 获取服务器当前信息
 * @Author : zq2599@gmail.com
 * @Date : 2018-01-31 11:02
 */
@RestController
public class ServerInfo {


    @RequestMapping(value = "/getserverinfo", method = RequestMethod.GET)
    public String getUserInfoWithRequestParam(){
        return String.format("server : %s, time : %s", getIPAddr(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    }

    /**
     * 获取本机IP地址
     * @return
     */
    private static String getIPAddr(){
        String hostAddress = null;
        try{
            InetAddress address = InetAddress.getLocalHost();
            hostAddress = address.getHostAddress();
        }catch (UnknownHostException e){
            e.printStackTrace();
        }

        return hostAddress;
    }
}
