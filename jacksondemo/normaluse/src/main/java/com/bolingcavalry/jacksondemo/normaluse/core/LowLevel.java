package com.bolingcavalry.jacksondemo.normaluse.core;

import com.bolingcavalry.jacksondemo.beans.TwitterEntry;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @Description: jackson低阶方法的使用
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/7/4 15:50
 */
public class LowLevel {
    JsonFactory jsonFactory = new JsonFactory();




    public TwitterEntry read() throws Exception {
        String rawJSONStr = "{\n" +
                "  \"id\":1125687077,\n" +
                "  \"text\":\"@stroughtonsmith You need to add a \\\"Favourites\\\" tab to TC/iPhone. Like what TwitterFon did. I can't WAIT for your Twitter App!! :) Any ETA?\",\n" +
                "  \"fromUserId\":855523, \n" +
                "  \"toUserId\":815309,\n" +
                "  \"languageCode\":\"en\"\n" +
                "}";

        JsonParser jsonParser = jsonFactory.createParser(rawJSONStr);

        if (jsonParser.nextToken() != JsonToken.START_OBJECT) {
            throw new IOException("Expected data to start with an Object");
        }

        TwitterEntry result = new TwitterEntry();

        try{
        // Iterate over object fields:
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {

            String fieldName = jsonParser.getCurrentName();

            System.out.println("----------------:" + jsonParser.getCurrentName());

            // 解析下一个
            jsonParser.nextToken();

            switch (fieldName) {
                case "id":
                    result.setId(jsonParser.getLongValue());
                    break;
                case "text":
                    result.setText(jsonParser.getText());
                    break;
                case "fromUserId":
                    result.setFromUserId(jsonParser.getIntValue());
                    break;
                case "toUserId":
                    result.setToUserId(jsonParser.getIntValue());
                    break;
                case "languageCode":
                    result.setLanguageCode(jsonParser.getText());
                    break;
                default:
                    throw new IOException("Unrecognized field '" + jsonParser.getCurrentName() + "'");

            }
        }
    } catch (Exception e) {
            System.out.println("parse exception : " + e);
        }finally {
            jsonParser.close(); // important to close both parser and underlying File reader
        }

        return result;
    }

    public static void main(String[] args) throws Exception{

        LowLevel lowLevel = new LowLevel();

        System.out.println(lowLevel.read());

    }
}
