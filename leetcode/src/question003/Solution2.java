package question003;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/1/6 18:32
 */
public class Solution2 {

    public int lengthOfLongestSubstring(String s) {
        int left =0, right =0, max = 0;
        Map<Character,Integer> map = new HashMap<>();
        while (right<s.length()){
            if(map.containsKey(s.charAt(right))){
                int pos = map.get(s.charAt(right));
                if(pos>=left){
                    left = pos + 1;
                }
            }
            map.put(s.charAt(right), right++);
            if((right-left)>max){
                max = right-left;
            }
        }

        return max;
    }

    public static void main(String[] args) {
        System.out.println(new Solution2().lengthOfLongestSubstring("abcabcbb"));
    }
}
