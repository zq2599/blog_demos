package com.bolingcavalry.jacksondemo.beans;

public class TwitterEntry {

    /**
     * 推特消息id
     */
    long id;

    /**
     * 消息内容
     */
    String text;

    /**
     * 消息创建者
     */
    int fromUserId;

    /**
     * 消息接收者
     */
    int toUserId;

    /**
     * 语言类型
     */
    String languageCode;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(int fromUserId) {
        this.fromUserId = fromUserId;
    }

    public int getToUserId() {
        return toUserId;
    }

    public void setToUserId(int toUserId) {
        this.toUserId = toUserId;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public TwitterEntry() {
    }

    public String toString() {
        return "[Tweet, id: "+id+", text='"+text+"', from: "+fromUserId+", to: "+toUserId+", lang: "+languageCode+"]";
    }
}
