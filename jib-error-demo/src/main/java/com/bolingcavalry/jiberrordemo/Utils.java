package com.bolingcavalry.jiberrordemo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static String time(){
      return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).toString();
    }

    public static void main(String[] args){
        System.out.println(time());
    }
}
