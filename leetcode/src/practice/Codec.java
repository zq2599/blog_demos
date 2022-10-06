package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class Codec {

    private static final String PREFIX = "http://tinyurl.com/";

    private List<String> list = new ArrayList<>();

    // Encodes a URL to a shortened URL.
    public String encode(String longUrl) {
        list.add(longUrl);
        return PREFIX + (list.size()-1);
    }

    // Decodes a shortened URL to its original URL.
    public String decode(String shortUrl) {
        return list.get(Integer.parseInt(shortUrl.substring(PREFIX.length())));
    }


    public static void main(String[] args) {
        Codec codec = new Codec();
        String s = codec.encode("abc");
        System.out.println(s);
        System.out.println(codec.decode(s));
    }
}
