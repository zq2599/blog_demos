package practice;

import java.util.Arrays;

/**
 * @program: leetcode
 * @description: 快排
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0322 {


    /*
    public int coinChange(int[] coins, int amount) {
        // 处理异常
        if(amount<1) {
            return 0;
        }

        if (null==coins || coins.length<1) {
            return -1;
        }

        // 这个数组的下标代表金额，值代表凑齐此金额的最少硬币数量
        // nums[0]代表凑齐0元所需最少硬币数，
        // nums[10]代表凑齐10元所需最少硬币数，
        // 再次提醒，用数组下标代表金额，这个下标的含义非常重要
        int[] nums = new int[amount+1];

        // 注意，要从1开始，因为num[0]一定等于0
        for(int i=1;i<nums.length;i++) {
            nums[i] = -1;
            //
            // 假设硬币有1,3,5三种，若i=10，那么10块钱有以下可能组成：
            // 1. 1块+9块 : 此时nums[10]=1+nums[9] --1代表一枚硬币，面值1元
            // 2. 3块+7块 : 此时nums[10]=1+nums[7] --1代表一枚硬币，面值3元
            // 3. 5块+5块 : 此时nums[10]=1+nums[5] --1代表一枚硬币，面值5元

            for(int j=0;j<coins.length;j++) {
                // 注意整篇代码和核心：nums[i-coins[j]]，
                // 首先，coins[j]，代表选定硬币的金额，例如是3元硬币，
                // 假设i=10，那么i-coins[j]的意思，就是选中3元硬币后，还剩7元，
                // 所以nums[i-coins[j]]就是nums[7]，也就是总量7元的时候所需的最少硬币数
                // 还要注意三个异常条件：
                // 1. 如果硬币面值大于指定金额，就不能选择此硬币，对应的数量就是-1，在择优的时候会跳过，
                // 2. 假设现在算的是凑齐3块钱所需的硬币，那么5块的硬币就不能参与了
                // 3. 假设现在算的是凑齐3块钱所需的硬币，如果选择1元硬币，那就需要3-1=2元硬币的最小硬币数，也就是nums[2]，如果nums[2]=-1，那么就不能选择1元硬币了
                if (coins[j]>amount || i<coins[j] || nums[i-coins[j]]<0) {
                    continue;
                } else if(-1==nums[i] || nums[i]>(nums[i-coins[j]] + 1)){
                    // 将整个tempNums数组中最小的数字放入nums[i]中
                    nums[i] = nums[i-coins[j]] + 1;
                }
            }
        }

        return nums[amount];
    }
    */

    public int coinChange(int[] coins, int amount) {
        // 处理异常
        if(amount<1) {
            return 0;
        }

        if (null==coins || coins.length<1) {
            return -1;
        }

        // 这个数组的下标代表金额，值代表凑齐此金额的最少硬币数量
        // nums[0]代表凑齐0元所需最少硬币数，
        // nums[10]代表凑齐10元所需最少硬币数，
        // 再次提醒，用数组下标代表金额，这个下标的含义非常重要
        int[] nums = new int[amount+1];

        // 全部放上最大值，这样择优的时候只要取最小值即可
        Arrays.fill(nums, Integer.MAX_VALUE);

        // 凑齐0元，需要0个硬币
        nums[0] = 0;

        // 注意，要从1开始，因为num[0]一定等于0
        for(int i=1;i<nums.length;i++) {
            // 假设硬币有1,3,5三种，若i=10，那么10块钱有以下可能组成：
            // 1. 1块+9块 : 此时nums[10]=1+nums[9] --1代表一枚硬币，面值1元
            // 2. 3块+7块 : 此时nums[10]=1+nums[7] --1代表一枚硬币，面值3元
            // 3. 5块+5块 : 此时nums[10]=1+nums[5] --1代表一枚硬币，面值5元

            for(int j=0;j<coins.length;j++) {
                // 注意整篇代码和核心：nums[i-coins[j]]，
                // 首先，coins[j]，代表选定硬币的金额，例如是3元硬币，
                // 假设i=10，那么i-coins[j]的意思，就是选中3元硬币后，还剩7元，
                // 所以nums[i-coins[j]]就是nums[7]，也就是总量7元的时候所需的最少硬币数
                // 还要异常条件：假设现在算的是凑齐3块钱所需的硬币，那么5块的硬币就不能参与了
                if (i>=coins[j] && nums[i-coins[j]]!=Integer.MAX_VALUE) {
                    nums[i] = Math.min(nums[i], nums[i-coins[j]] + 1);
                }
            }
        }

        return nums[amount]==Integer.MAX_VALUE ? -1 : nums[amount];
    }


    public static void main(String[] args) {
        System.out.println(new L0322().coinChange(new int[] {2}, 3));
    }
}
