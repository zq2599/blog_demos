package com.bolingcavalry.serviceprovider.controller;

import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Description : 对外服务的controller
 * @Author : zq2599@gmail.com
 * @Date : 2018-01-24 9:17
 */
@RestController
public class UserController {
    @RequestMapping(value = "/getuserinfo/{id}", method = RequestMethod.GET)
    public String getUserInfo(@PathVariable String id){
        return String.format("user[%s]", id);
    }

    @RequestMapping(value = "/getuserinfo", method = RequestMethod.GET)
    public String getUserInfoWithRequestParam(@RequestParam("id") String id, @RequestParam("name") String name){
        return String.format("user [%s], id [%s], from server [%s]", name, id, getIPAddr());
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
