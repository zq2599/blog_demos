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
     * @param topic
     * @param message
     */
    void sendSimpleMsg(String topic, String message);

    /**
     * 发送一个带key的消息，这样就能指定partition了
     * @param topic
     * @param key
     * @param message
     */
    void sendKeyMsg(String topic, String key, String message);



}
