package com.bolingcavalry.sparkdemo.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @Description: 常用的静态工具放置在此
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/2/16 9:01
 */
public class Tools {

    /**
     * 域名的格式化模板
     */
    private static final String URL_TEMPALTE = "https://%s/wiki/%s";

    /**
     * 根据项目名称和三级域名还原完整url，
     * 还原逻辑来自：https://wikitech.wikimedia.org/wiki/Analytics/Archive/Data/Pagecounts-raw
     * @param project
     * @param thirdLvPath
     * @return
     */
    public static String getUrl(String project, String thirdLvPath){
        //如果入参不合法，就返回固定格式的错误提示
        if(StringUtils.isBlank(project) || StringUtils.isBlank(thirdLvPath)){
            return "1. invalid param (" + project + ")(" + thirdLvPath + ")";
        }

        //检查project中是否有"."
        int dotOffset = project.indexOf('.');

        //如果没有"."，就用project+".wikipedia.org"作为一级域名
        if(dotOffset<0){
            return String.format(URL_TEMPALTE,
                    project + ".wikipedia.org",
                    thirdLvPath);
        }

        //如果有"."，就用"."之后的字符串按照不同的逻辑转换
        String code = project.substring(dotOffset);

        //".mw"属于移动端网页，统计的逻辑略微复杂，详情参考网页链接，这里不作处理直接返回固定信息
        if(".mw".equals(code)){
            return "mw page (" + project + ")(" + thirdLvPath + ")";
        }

        String firstLvPath = null;

        //就用"."之后的字符串按照不同的逻辑转换
        switch(code){
            case ".b":
                firstLvPath = ".wikibooks.org";
                break;
            case ".d":
                firstLvPath = ".wiktionary.org";
                break;
            case ".f":
                firstLvPath = ".wikimediafoundation.org";
                break;
            case ".m":
                firstLvPath = ".wikimedia.org";
                break;
            case ".n":
                firstLvPath = ".wikinews.org";
                break;
            case ".q":
                firstLvPath = ".wikiquote.org";
                break;
            case ".s":
                firstLvPath = ".wikisource.org";
                break;
            case ".v":
                firstLvPath = ".wikiversity.org";
                break;
            case ".voy":
                firstLvPath = ".wikivoyage.org";
                break;
            case ".w":
                firstLvPath = ".mediawiki.org";
                break;
            case ".wd":
                firstLvPath = ".wikidata.org";
                break;
        }

        if(null==firstLvPath){
            return "2. invalid param (" + project + ")(" + thirdLvPath + ")";
        }

        //还原地址
        return String.format(URL_TEMPALTE,
                project.substring(0, dotOffset) + firstLvPath,
                thirdLvPath);
    }



    public static void main(String[] args){
        String str = "abc.123456";

        System.out.println(str.substring(str.indexOf('.')));
    }
}
