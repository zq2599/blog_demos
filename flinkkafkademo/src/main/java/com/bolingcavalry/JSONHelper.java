package com.bolingcavalry;

import com.alibaba.fastjson.JSONObject;

/**
 * @Description: 解析原始消息的辅助类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/1/1 20:13
 */
public class JSONHelper {

    /**
     * 解析消息，得到时间字段
     * @param raw
     * @return
     */
    public static long getTimeLongFromRawMessage(String raw){
        SingleMessage singleMessage = parse(raw);
        return null==singleMessage ? 0L : singleMessage.getTimeLong();
    }

    /**
     * 将消息解析成对象
     * @param raw
     * @return
     */
    public static SingleMessage parse(String raw){
        SingleMessage singleMessage = null;

        if (raw != null) {
            singleMessage = JSONObject.parseObject(raw, SingleMessage.class);
        }

        return singleMessage;
    }
}
