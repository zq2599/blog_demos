package com.bolingcavalry.sparkdemo.bean;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @Description: 数据结构类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/2/10 15:33
 */
public class PageInfo implements Serializable {
    /**
     * 还原的url地址
     */
    private String url;

    /**
     * urldecode之后的三级域名
     */
    private String name;

    /**
     * 该三级域名的请求次数
     */
    private int requestTimes;

    /**
     * 该地址被请求的字节总数
     */
    private long requestLength;

    /**
     * 对应的原始字段
     */
    private List<String> raws = new LinkedList<>();


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRequestTimes() {
        return requestTimes;
    }

    public void setRequestTimes(int requestTimes) {
        this.requestTimes = requestTimes;
    }

    public long getRequestLength() {
        return requestLength;
    }

    public void setRequestLength(long requestLength) {
        this.requestLength = requestLength;
    }

    public List<String> getRaws() {
        return raws;
    }

    public void setRaws(List<String> raws) {
        this.raws = raws;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
