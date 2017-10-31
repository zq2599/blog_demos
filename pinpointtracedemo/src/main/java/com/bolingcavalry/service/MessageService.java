package com.bolingcavalry.service;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 发送消息的服务接口
 * @email zq2599@gmail.com
 * @Date 2017/10/28 上午9:57
 */
public interface MessageService {
    /**
     * 发送一个普通的消息
     * @param ip
     * @param port
     * @param content
     */
    String sendSimpleMsg(String ip, String port, String content);
}
