package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0918 {

    public int maxSubarraySumCircular(int[] nums) {
        // dp[i]定义：选定nums[i]的连续最小数组
        int dp = nums[0];
        int min = nums[0];
        int total = nums[0];

        for(int i=1;i<nums.length;i++) {
            dp = Math.min(dp+nums[i], nums[i]);
            min = Math.min(min, dp);
            total += nums[i];
        }

        // 如果最小值的子串大于0，只能说明整个数组都是整数，那么整个数组可以连起来
        if (min>=0) {
            return total;
        }

        // 再算最大
        int max= nums[0];
        dp = nums[0];

        for(int i=1;i<nums.length;i++) {
            dp = Math.max(dp+nums[i], nums[i]);
            max = Math.max(max, dp);
        }

        // 特殊情况：全负数
        if (total==min) {
            return max;
        }


        return Math.max(max, total-min);
    }

    public static void main(String[] args) {
//        System.out.println(new L0918().maxSubarraySumCircular(new int[]{1,-2,3,-2})); // 3
//        System.out.println(new L0918().maxSubarraySumCircular(new int[]{5,-3,5})); // 10
//        System.out.println(new L0918().maxSubarraySumCircular(new int[]{3,-2,2,-3})); // 3
        System.out.println(new L0918().maxSubarraySumCircular(new int[]{-3,-2,-3})); // 3
    }
}
