package practice;

import java.util.Arrays;
import java.util.TreeSet;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0198 {

    /*
    public int rob(int[] nums) {
        if (1==nums.length) {
            return nums[0];
        }
        // dp[i]定义：一定打劫第i家的时候，所获取的最大金额
        int dp[] = new int[nums.length];
        dp[0] = nums[0];
        dp[1] = nums[1];

        // prevMax[i]的定义，从0-i的dp数组中的最大值
        int[] prevMax = new int[nums.length];
        prevMax[0] = dp[0];
        prevMax[1] = Math.max(prevMax[0], dp[1]);

        for (int i=2;i<nums.length;i++) {
            dp[i] = nums[i] + prevMax[i-2];
            prevMax[i] = Math.max(prevMax[i-1], dp[i]);
        }

        return prevMax[nums.length-1];
    }
    */

    public int rob(int[] nums) {
        if (1==nums.length) {
            return nums[0];
        }
        // dp[i]定义：一定打劫第i家的时候，所获取的最大金额
        int dp;

        // prevMax[i]的定义，从0-i的dp数组中的最大值
        int prevPrevMax = nums[0];
        int prevMax = Math.max(nums[0], nums[1]);

        for (int i=2;i<nums.length;i++) {
            dp = nums[i] + prevPrevMax;
            prevPrevMax = prevMax;
            prevMax = Math.max(prevMax, dp);
        }

        return prevMax;
    }


    public static void main(String[] args) {
        System.out.println(new L0198().rob(new int[] {1,2,3,1})); // 4
        System.out.println(new L0198().rob(new int[] {2,7,9,3,1})); // 12
    }
}
