package question001;

import java.util.*;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/1/6 16:15
 */
public class Solution {

    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> all = new HashMap<>();

        int index = 0;
        for(int i :nums){

            if(all.containsKey(target-i)){
                return new int[] {all.get(target-i), index};
            }

            all.put(i, index);

            index++;
        }

        return null;
    }

    public static void main(String[] args){
        int[] rlt = new Solution().twoSum(new int[]{2, 7, 11, 15}, 9);
        for(int i : rlt){
            System.out.println(i);
        }
    }
}
