package practice;

import java.util.Arrays;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L1624 {

    public int maxLengthBetweenEqualCharacters(String s) {
        int max = -1;
        int[] firstPositions = new int[26];
        Arrays.fill(firstPositions, -2);
        char[] array = s.toCharArray();

        int offset;

        for (int i=0;i<array.length;i++) {
           offset = array[i]-'a';

           // 等于-1表示第一次出现该字符，就把该字符的位置记录在数组中
           if (-2==firstPositions[offset]) {
               firstPositions[offset] = i;
           } else {
               max = Math.max(max, i-firstPositions[offset]-1);
           }
        }

        return max;
    }


    public static void main(String[] args) {
        System.out.println(new L1624().maxLengthBetweenEqualCharacters("aa")); // 0
        System.out.println(new L1624().maxLengthBetweenEqualCharacters("abca")); // 2
        System.out.println(new L1624().maxLengthBetweenEqualCharacters("cbzxy")); // -1
        System.out.println(new L1624().maxLengthBetweenEqualCharacters("cabbac")); // 4
    }
}
