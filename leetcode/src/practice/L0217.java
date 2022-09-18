package practice;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0217 {

    /*
    public boolean containsDuplicate(int[] nums) {
        if (nums.length<2) {
            return false;
        }

        Arrays.sort(nums);

        for (int i=1;i<nums.length;i++) {
            if (nums[i]==nums[i-1]) {
                return true;
            }
        }

        return false;
    }
    */

    public boolean containsDuplicate(int[] nums) {
        Set<Integer> set = new HashSet<>();

        for (int i : nums) {
            if (set.contains(i)) {
                return true;
            }

            set.add(i);
        }

        return false;
    }


    public static void main(String[] args) {
        System.out.println(new L0217().containsDuplicate(new int[]{1,2,3,1})); // true
        System.out.println(new L0217().containsDuplicate(new int[]{1,2,3,4})); // false
        System.out.println(new L0217().containsDuplicate(new int[]{1,1,1,3,3,4,3,2,4,2})); // true
    }
}
