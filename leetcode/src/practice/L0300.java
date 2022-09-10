package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0300 {

    /*
    public int lengthOfLIS(int[] nums) {

        // dp[i]表示：以nums[i]作为结尾的最长子序列的长度，注意再注意，一定要以nums[i]结尾
        int[] dp = new int[nums.length];

        dp[0] = 1;
        int max = 1;

        for(int i=1;i<nums.length;i++) {
            // 所有比nums[i]小的数字，都可能用来连接nums[i]，
            // 以2,1,3为例，2连接3符合要求，1连接3也符合要求，
            // 这时候就要判断2连接3合适，还是1连接3合适
            for(int j=0;j<i;j++) {
                if(nums[j]<nums[i]) {
                    if(dp[j]>dp[i]) {
                        dp[i] = dp[j];
                    }
                }
            }

            // 加上自己这一位
            dp[i] += 1;

            // 所有dp中的最大值就是返回值
            if(max<dp[i]) {
                max = dp[i];
            }
        }

        return max;
    }
    */

    public int lengthOfLIS(int[] nums) {

        // dp[i]表示：以nums[i]作为结尾的最长子序列的长度，注意再注意，一定要以nums[i]结尾
        int[] dp = new int[nums.length];

        dp[0] = 1;
        int max = 1;

        for(int i=1;i<nums.length;i++) {
            // 所有比nums[i]小的数字，都可能用来连接nums[i]，
            // 以2,1,3为例，2连接3符合要求，1连接3也符合要求，
            // 这时候就要判断2连接3合适，还是1连接3合适
            for(int j=0;j<i;j++) {
                if(nums[j]<nums[i]) {
                    if(dp[j]>dp[i]) {
                        dp[i] = dp[j];
                    }
                }
            }

            // 加上自己这一位
            dp[i] += 1;

            // 所有dp中的最大值就是返回值
            if(max<dp[i]) {
                max = dp[i];
            }
        }

        return max;
    }




    public static void main(String[] args) {
        System.out.println(new L0300().lengthOfLIS(new int[]{10,9,2,5,3,7,101,18})); // 4
        System.out.println(new L0300().lengthOfLIS(new int[]{0,1,0,3,2,3})); // 4
        System.out.println(new L0300().lengthOfLIS(new int[]{7,7,7,7,7,7,7})); // 1
        System.out.println(new L0300().lengthOfLIS(new int[]{4,10,4,3,8,9})); // 3
        System.out.println(new L0300().lengthOfLIS(new int[]{0,1,0,3,2,3})); // 4
        System.out.println(new L0300().lengthOfLIS(new int[]{6,3,5,10,11,29,14,13,7,4,8,12})); // 4
    }
}
