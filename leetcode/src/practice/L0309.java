package practice;

import java.util.Arrays;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0309 {

    public int maxProfit(int[] prices) {

        // 0 : 手里持有股票
        // 1 : 手里不持有，未在冷冻期
        // 2 : 手里不持有，且在冷冻期
        int[][] dp = new int[prices.length][3];

        dp[0][0] = -prices[0];
        dp[0][1] = 0;
        dp[0][2] = 0;


        for (int i=0;i<prices.length;i++) {

            // 二选一：啥也没做，或者之前处于未在冷冻期状态，当天买入
            dp[i][0] = Math.max(dp[i-1][0], dp[i-1][1]-prices[i]);
            // 二选一：啥也没做，或者之前处于冷冻期，今天就变成了未在冷冻期
            dp[i][1] = Math.max(dp[i-1][1], dp[i-1][2]);
            // 当天处于冷冻期，一定是因为前一天售出了股票
            dp[i][1] = dp[i-1][0];
        }


        return Math.max(dp[prices.length-1][1], dp[prices.length-1][2]);

    }



    public static void main(String[] args) {
        System.out.println(new L0309().maxProfit(new int[]{1,2,3,0,2})); // 3
        System.out.println(new L0309().maxProfit(new int[]{1})); // 0
    }
}