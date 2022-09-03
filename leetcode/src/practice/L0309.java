package practice;

import java.util.Arrays;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0309 {

    /*
    public int maxProfit(int[] prices) {

        // 0 : 手里持有股票
        // 1 : 手里不持有，未在冷冻期
        // 2 : 手里不持有，且在冷冻期
        int[][] dp = new int[prices.length][3];

        dp[0][0] = -prices[0];
        dp[0][1] = 0;
        dp[0][2] = 0;


        for (int i=1;i<prices.length;i++) {

            // 二选一：啥也没做，或者之前处于未在冷冻期状态，当天买入
            dp[i][0] = Math.max(dp[i-1][0], dp[i-1][1]-prices[i]);
            // 二选一：啥也没做，或者之前处于冷冻期，今天就变成了未在冷冻期
            dp[i][1] = Math.max(dp[i-1][1], dp[i-1][2]);
            // 当天股市结束后不持有股票，且处于冷冻期，一定是因为当天卖出了(一定要注意dp的定义是：当前股市结束后的状态！)
            dp[i][2] = dp[i-1][0] + prices[i];
        }


        return Math.max(dp[prices.length-1][1], dp[prices.length-1][2]);
    }
    */

    public int maxProfit(int[] prices) {
        // 昨日，股市结束后，手里持有股票状态，最大利润
        int lastWithStock = -prices[0];
        // 昨日，股市结束后，手里不持有股票状态，不在冷冻期，最大利润
        int lastWithOutStockNotInFrozen = 0;
        // 昨日，股市结束后，手里不持有股票状态，在冷冻期，最大利润
        int lastWithOutStockInFrozen = 0;

        // 当日，股市结束后，手里持有股票状态，最大利润
        int curWithStock;
        // 当日，股市结束后，手里不持有股票状态，不在冷冻期，最大利润
        int curWithOutStockNotInFrozen = 0;
        // 当日，股市结束后，手里不持有股票状态，在冷冻期，最大利润
        int curWithOutStockInFrozen = 0;


        for (int i=1;i<prices.length;i++) {
            // 二选一：啥也没做，或者之前处于不在冷冻期状态，当天买入
            curWithStock = Math.max(lastWithStock, lastWithOutStockNotInFrozen-prices[i]);
            // 二选一：啥也没做，或者之前处于冷冻期，今天就变成了未在冷冻期
            curWithOutStockNotInFrozen = Math.max(lastWithOutStockNotInFrozen, lastWithOutStockInFrozen);
            // 当天股市结束后不持有股票，且处于冷冻期，一定是因为当天卖出了(一定要注意dp的定义是：当前股市结束后的状态！)
            curWithOutStockInFrozen = lastWithStock + prices[i];

            // 更新，为下一次循环做准备
            lastWithStock = curWithStock;
            lastWithOutStockNotInFrozen = curWithOutStockNotInFrozen;
            lastWithOutStockInFrozen = curWithOutStockInFrozen;
        }

        return Math.max(curWithOutStockNotInFrozen, curWithOutStockInFrozen);
    }


    public static void main(String[] args) {
        System.out.println(new L0309().maxProfit(new int[]{1,2,3,0,2})); // 3
        System.out.println(new L0309().maxProfit(new int[]{1})); // 0
        System.out.println(new L0309().maxProfit(new int[]{1,2,4})); // 3
    }
}