package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0152 {
    /*
    public int maxProduct(int[] nums) {
        // dp[i][0]的含义：以nums[i]结尾的子数组的最大乘积
        // dp[i][1]的含义：以nums[i]结尾的子数组的最小乘积
        int[][] dp = new int[nums.length][2];

        dp[0][0] = nums[0];
        dp[0][1] = nums[0];
        int mulVal;
        int max = dp[0][0];
        for (int i=1;i<nums.length;i++) {

            if(nums[i]>=0) {
                dp[i][0] = dp[i-1][0]*nums[i]>nums[i] ? dp[i-1][0]*nums[i] : nums[i];
                dp[i][1] = dp[i-1][1]*nums[i]<nums[i] ? dp[i-1][1]*nums[i] : nums[i];
            } else {
                dp[i][0] = dp[i-1][1]*nums[i]>nums[i] ? dp[i-1][1]*nums[i] : nums[i];
                dp[i][1] = dp[i-1][0]*nums[i]<nums[i] ? dp[i-1][0]*nums[i] : nums[i];
            }

            if (max<dp[i][0]) {
                max = dp[i][0];
            }
        }

        return max;
    }
    */


    public int maxProduct(int[] nums) {
        int max = nums[0];
        int lastMax = nums[0];
        int lastMin = nums[0];
        int currentMax, currentMin;
        int maxMul, minMul;

        for (int i=1;i<nums.length;i++) {
            maxMul = lastMax*nums[i];
            minMul = lastMin*nums[i];

            if(nums[i]>=0) {
                currentMax = maxMul>nums[i] ? maxMul : nums[i];
                currentMin = minMul<nums[i] ? minMul : nums[i];
            } else {
                currentMax = minMul>nums[i] ? minMul : nums[i];
                currentMin = maxMul<nums[i] ? maxMul : nums[i];
            }

            lastMin = currentMin;
            lastMax = currentMax;

            if (max<currentMax) {
                max = currentMax;
            }
        }

        return max;
    }



    public static void main(String[] args) {
        System.out.println(new L0152().maxProduct(new int[]{2,3,-2,4})); // 6
        System.out.println(new L0152().maxProduct(new int[]{-2,0,-1})); // 0
        System.out.println(new L0152().maxProduct(new int[]{-2,3,-4})); // 24
    }
}
