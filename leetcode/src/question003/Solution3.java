package question003;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/1/6 18:32
 */
public class Solution3 {

    public int lengthOfLongestSubstring(String s) {
        int left =0, right =0, max = 0;

        int[] array = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                       -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1};

        int offset;
        while (right<s.length()){

            offset = s.charAt(right) - 32;

            if(array[offset]>-1){
                int pos = array[offset];

                if(pos>=left){
                    left = pos + 1;
                }
            }


            array[offset] = right++;

            if((right-left)>max){
                max = right-left;
            }
        }

        return max;
    }

    public static void main(String[] args) {
        System.out.println(new Solution3().lengthOfLongestSubstring("abcabcbb"));
        System.out.println(100 + ' ');
    }
}
