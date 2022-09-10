package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0673 {

    public int findNumberOfLIS(int[] nums) {
        // dp[i]表示：以nums[i]作为结尾的最长子序列的长度，注意再注意，一定要以nums[i]结尾
        int[] dp = new int[nums.length];
        int[] strNums = new int[nums.length];

        dp[0] = 1;
        strNums[0] = 1;
        int max = 1;
        int maxNum = 1;

        for(int i=1;i<nums.length;i++) {
            strNums[i] = 1;
            // 所有比nums[i]小的数字，都可能用来连接nums[i]，
            // 以2,1,3为例，2连接3符合要求，1连接3也符合要求，
            // 这时候就要判断2连接3合适，还是1连接3合适
            for(int j=0;j<i;j++) {
                if(nums[j]<nums[i]) {
                    // 发现一个更大的，就把dp[i]等于dp[j]，
                    // 并且由于是新的序列，这样的序列数量为1
                    if(dp[j]>dp[i]) {
                        dp[i] = dp[j];
                        strNums[i] = strNums[j];
                    } else if (dp[j]==dp[i]) {
                        // 以1,3,5,4,7为例
                        // 1,3,5和7连接，是最长的，长度是4
                        // 1,3,4和7连接，长度也等于4，所以，这样的长度有两个，于是strNums要加在一起
                        strNums[i]+= strNums[j];
                    }
                }
            }

            // 加上自己这一位
            dp[i] += 1;

            // 所有dp中的最大值就是最常子序列
            if(max<dp[i]) {
                max = dp[i];
                maxNum = strNums[i];
            } else if (max==dp[i]) {
                maxNum += strNums[i];
            }
        }

        return maxNum;
    }

    public static void main(String[] args) {
        System.out.println(new L0673().findNumberOfLIS(new int[]{1,3,5,4,7})); // 2
        System.out.println(new L0673().findNumberOfLIS(new int[]{2,2,2,2,2})); // 5
        System.out.println(new L0673().findNumberOfLIS(new int[]{1,2,4,3,5,4,7,2})); // 3
        System.out.println(new L0673().findNumberOfLIS(new int[]{1,1,1,2,2,2,3,3,3})); // 27
    }
}
