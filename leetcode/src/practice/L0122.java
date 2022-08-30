package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0122 {

    /*
    public int maxProfit(int[] prices) {

        int[][] dp = new int[prices.length][2];

        // 第0天股市结束后，如果手里没有股票，那就是没有购买过，此时最大利润只能等于0
        // dp[0][0] = 0;

        // 第0天股市结束后，如果手里有股票，那就是当前购买的，此时最大利润就是负数
        dp[0][1] = -prices[0];

        for (int i=1;i<prices.length;i++) {
            dp[i][0] = Math.max(dp[i-1][0], dp[i-1][1] + prices[i]);
            dp[i][1] = Math.max(dp[i-1][1], dp[i-1][0] - prices[i]);
        }

        // 第i天结束后，手里不持有股票的最大利润就是返回值
        return dp[prices.length-1][0];
    }
    */

    /*
    public int maxProfit(int[] prices) {
        // 第0天股市结束后，如果手里有股票，那就是当前购买的，此时最大利润就是负数
        int prevWithStock = -prices[0];

        // 第0天股市结束后，如果手里没有股票，那就是没有购买过，此时最大利润只能等于0
        int prevWithoutStock = 0;

        // 当天股市结束后，如果手里有股票时的最大利润
        int currentWithStock;

        // 当天股市结束后，如果手里没有股票时的最大利润
        int currentWithoutStock = 0;

        for (int i=1;i<prices.length;i++) {
            currentWithoutStock = Math.max(prevWithoutStock, prevWithStock + prices[i]);
            currentWithStock = Math.max(prevWithStock, prevWithoutStock - prices[i]);
            prevWithStock = currentWithStock;
            prevWithoutStock = currentWithoutStock;
        }

        // 第i天结束后，手里不持有股票的最大利润就是返回值
        return currentWithoutStock;
    }
    */

    public int maxProfit(int[] prices) {
        if (prices.length<2) {
            return 0;
        }

        int total = 0;

        for (int i=1;i<prices.length;i++) {

            if (prices[i]>prices[i-1]) {
                total += prices[i] - prices[i-1];
            }

        }

        return total;
    }

    public static void main(String[] args) {
        System.out.println(new L0122().maxProfit(new int[]{7,1,5,3,6,4})); // 7
        System.out.println(new L0122().maxProfit(new int[]{1,2,3,4,5})); // 4
        System.out.println(new L0122().maxProfit(new int[]{7,6,4,3,1})); // 0
    }
}
