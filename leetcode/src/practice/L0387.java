package practice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0387 {

    /*
    public int firstUniqChar(String s) {
        int[] firstAppearPos = new int[26];
        char[] array = s.toCharArray();
        int offset;

        for (int i=0;i<array.length;i++) {
            offset = array[i]-'a';

            // 等于0表示从未出现过
            if (0==firstAppearPos[offset]) {
                // 记录该字母首次出现的位置+1
                firstAppearPos[offset] = i+1;
            } else if (firstAppearPos[offset]>0) {
                // 大于0表示已经出现过了，
                // 此时将其改为负数，表示并非首次出现
                firstAppearPos[offset] = -1;
            }
        }

        int rlt = Integer.MAX_VALUE;

        for (int i = 0; i < firstAppearPos.length; i++) {
            if (firstAppearPos[i]>0) {
                rlt = Math.min(rlt, firstAppearPos[i]-1);
            }
        }

        return Integer.MAX_VALUE==rlt ? -1 : rlt;
    }
    */

    public int firstUniqChar(String s) {
        int[] firstAppearPos = new int[26];
        int offset;

        for (int i=0;i<s.length();i++) {
            offset = s.charAt(i)-'a';

            // 等于0表示从未出现过
            if (0==firstAppearPos[offset]) {
                // 记录该字母首次出现的位置+1
                firstAppearPos[offset] = i+1;
            } else if (firstAppearPos[offset]>0) {
                // 大于0表示已经出现过了，
                // 此时将其改为负数，表示并非首次出现
                firstAppearPos[offset] = -1;
            }
        }

        int rlt = Integer.MAX_VALUE;

        for (int i = 0; i < firstAppearPos.length; i++) {
            if (firstAppearPos[i]>0) {
                rlt = Math.min(rlt, firstAppearPos[i]-1);
            }
        }

        return Integer.MAX_VALUE==rlt ? -1 : rlt;
    }



    public static void main(String[] args) {
        System.out.println(new L0387().firstUniqChar("leetcode")); // 0
        System.out.println(new L0387().firstUniqChar("loveleetcode")); // 2
        System.out.println(new L0387().firstUniqChar("aabb")); // -1
    }
}
