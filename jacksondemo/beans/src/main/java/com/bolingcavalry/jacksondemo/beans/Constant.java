package com.bolingcavalry.jacksondemo.beans;

/**
 * @Description: 一些常量
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/7/5 11:47
 */
public class Constant {
    /**
     * 该字符串的值是个网络地址，该地址对应的内容是个JSON
     */
    public final static String TEST_JSON_DATA_URL = "https://raw.githubusercontent.com/zq2599/blog_demos/master/files/twitteer_message.json";

    /**
     * 用来验证反序列化的JSON字符串
     */
    public final static String TEST_JSON_STR = "{\n" +
            "  \"id\":1125687077,\n" +
            "  \"text\":\"@stroughtonsmith You need to add a \\\"Favourites\\\" tab to TC/iPhone. Like what TwitterFon did. I can't WAIT for your Twitter App!! :) Any ETA?\",\n" +
            "  \"fromUserId\":855523, \n" +
            "  \"toUserId\":815309,\n" +
            "  \"languageCode\":\"en\"\n" +
            "}";

    /**
     * 用来验证序列化的TwitterEntry实例
     */
    public final static TwitterEntry TEST_OBJECT = new TwitterEntry();

    /**
     * 准备好TEST_OBJECT对象的各个参数
     */
    static {
        TEST_OBJECT.setId(123456L);
        TEST_OBJECT.setFromUserId(101);
        TEST_OBJECT.setToUserId(102);
        TEST_OBJECT.setText("this is a message for serializer test");
        TEST_OBJECT.setLanguageCode("zh");
    }

}
