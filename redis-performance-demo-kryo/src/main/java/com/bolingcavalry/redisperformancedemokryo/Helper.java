package com.bolingcavalry.redisperformancedemokryo;

import com.bolingcavalry.redisperformancedemokryo.bean.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description : 帮助类，封装了一些静态方法
 * @Author : zq2599@gmail.com
 * @Date : 2018-06-10 21:36
 */
public class Helper {
    private static final Logger logger = LoggerFactory.getLogger(Helper.class);
    /**
     * 增加判空，再加上后缀
     * @param str
     * @return
     */
    public static String addSplitChar(String str){
        return (null==str ? "" : str) + "_";
    }

    /**
     * 把Person的所有字段拼接起来，计算一个MD5
     * @param person
     * @return
     */
    public static String md5(Person person){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(addSplitChar(String.valueOf(person.getId())))
                .append(addSplitChar(person.getName()))
                .append(addSplitChar(person.getSex()))
                .append(addSplitChar(String.valueOf(person.getAge())))
                .append(addSplitChar(person.getPhone()))
                .append(addSplitChar(person.getAddress()));

        String md5 = null;

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            BASE64Encoder base64en = new BASE64Encoder();
            md5 = base64en.encode(messageDigest.digest(stringBuilder.toString().getBytes("utf-8")));
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }

        return md5;
    }

    /**
     * 构建一个Person实例
     * @param persionIdGenerator
     * @return
     */
    public static Person buildPerson(AtomicInteger persionIdGenerator){
        Person person = new Person();

        person.setId(persionIdGenerator.incrementAndGet());

        String currentTime = String.valueOf(System.currentTimeMillis());

        person.setAddress("addres_" + currentTime);
        person.setAge(new Random().nextInt(30));
        person.setName("name" + currentTime);
        person.setPhone("phone_" + currentTime);
        person.setSex(String.valueOf(new Random().nextInt(2)));

        //计算MD5并保存在一个字段中
        person.setMd5(md5(person));

        return person;
    }

    /**
     * 将返回码和返回内容写入response
     * @param response
     * @param status
     * @param desc
     */
    public static void response(HttpServletResponse response, int status, String desc){
        if(null==response){
            logger.error("invalid response");
            return;
        }

        response.setStatus(status);
        try {
            response.getWriter().append(desc);
        }catch(IOException e){
            logger.error("get writer error, ", e);
        }
    }

    /**
     * 返回成功信息
     * @param response
     * @param desc
     */
    public static void success(HttpServletResponse response, String desc){
        response(response, 200, desc);
    }

    /**
     * 返回失败信息
     * @param response
     * @param desc
     */
    public static void error(HttpServletResponse response, String desc){
        response(response, 500, desc);
    }

    /**
     * 检查MD5
     * @param person
     * @return null：检查通过 非空：检查不通过，返回为错误信息
     */
    public static String checkPerson(Person person){
        if(null==person){
            return "invalid person";
        }

        if(StringUtils.isEmpty(person.getMd5())){
            return "md5 is invalid";
        }

        if(person.getMd5().equals(Helper.md5(person))){
            return null;
        }else {
            return("check md5 fail!");
        }
    }
}
