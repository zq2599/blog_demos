package question003;

import java.util.HashSet;
import java.util.Set;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/1/6 20:32
 */
public class Solution1 {

    public int lengthOfLongestSubstring(String s) {
        //窗口的起始位置,窗口的结束为止,最长记录
        int left = 0, right = 0, max = 0;

        //表示窗口内有哪些值
        Set<Character> set = new HashSet<>();

        while (right < s.length()) {
            //例如"abcdc"，窗口内是"abcd"，此时right等于[4],
            //发现窗口内有array[right]的值，就缩减窗口左边，
            //缩到窗内没有array[right]的值为止，
            //当left一路变大，直到left=3的时候，窗口内已经没有array[right]的值了
            if (set.contains(s.charAt(right))) {
                //left++表示左侧窗口缩减
                set.remove(s.charAt(left++));
            } else {
                //窗口内没有array[right]的时候，就把array[right]的值放入set中，表示当前窗口内有哪些值
                set.add(s.charAt(right++));

                if ((right - left) > max) {
                    max = right - left;
                }
            }
        }

        return max;
    }

    public static void main(String[] args) {
        System.out.println(new Solution1().lengthOfLongestSubstring("abcabcbb"));
    }
}