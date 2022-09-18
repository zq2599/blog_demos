package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0213 {

    private int rob(int[] nums, int start, int end) {
        if (start==end) {
            return nums[start];
        }

        // dp[i]定义：一定打劫第i家的时候，所获取的最大金额
        int dp;

        // prevMax[i]的定义，从0-i的dp数组中的最大值
        int prevPrevMax = nums[start];
        int prevMax = Math.max(nums[start], nums[start+1]);

        for (int i=start+2;i<=end;i++) {
            dp = nums[i] + prevPrevMax;
            prevPrevMax = prevMax;
            prevMax = Math.max(prevMax, dp);
        }

        return prevMax;
    }

    public int rob(int[] nums) {
        if (1==nums.length) {
            return nums[0];
        }
        return Math.max(
                rob(nums, 0, nums.length-2),
                rob(nums, 1, nums.length-1)
        );
    }


    public static void main(String[] args) {
        System.out.println(new L0213().rob(new int[] {2,3,2})); // 3
        System.out.println(new L0213().rob(new int[] {1,2,3,1})); // 4
        System.out.println(new L0213().rob(new int[] {1,2,3})); // 3
    }
}
