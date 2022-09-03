package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0714 {

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

    /*
    public int maxProfit(int[] prices, int fee) {
        // 0 : 第i天股市结束时，手里不持有股票，此时的最大利润
        // 1 : 第i天股市结束时，手里持有股票，此时的最大利润
        int[][] dp = new int[prices.length][2];

        // 第0天，不可能买入再卖出，此时手里不持有股票，只有一个可能就是啥也么做
        dp[0][0] = 0;
        // 第0天，此时手里持有股票，只有一个可能就是当天买的
        dp[0][1] = -prices[0];

        for (int i=1;i< prices.length;i++) {
            // 手里不持有股票的原因，二选一：要么昨天就不持有今天啥也么做，要么昨天持有今天卖出，此时要加上今天的股价再减去手续费
            dp[i][0] = Math.max(dp[i-1][0], dp[i-1][1] + prices[i] - fee);
            // 手里持有股票的原因，二选一：要么昨天就持有今天啥也么做，要么昨天不持有今天买入，此时要减去今天的股价
            dp[i][1] = Math.max(dp[i-1][1], dp[i-1][0] - prices[i]);
        }

        return dp[prices.length-1][0];
    }
    */

    /*
    public int maxProfit(int[] prices, int fee) {
        // 第0天，不可能买入再卖出，此时手里不持有股票，只有一个可能就是啥也么做
        int lastWithoutStock = 0;
        int currentWithoutStock = 0;
        // 第0天，此时手里持有股票，只有一个可能就是当天买的
        int withStock = -prices[0];

        for (int i=1;i< prices.length;i++) {
            // 手里不持有股票的原因，二选一：要么昨天就不持有今天啥也么做，要么昨天持有今天卖出，此时要加上今天的股价再减去手续费
            currentWithoutStock = Math.max(lastWithoutStock, withStock + prices[i] - fee);
            // 手里持有股票的原因，二选一：要么昨天就持有今天啥也么做，要么昨天不持有今天买入，此时要减去今天的股价
            withStock = Math.max(withStock, lastWithoutStock - prices[i]);

            lastWithoutStock = currentWithoutStock;
        }

        return currentWithoutStock;
    }
    */

    public int maxProfit(int[] prices, int fee) {
        int buy = prices[0] + fee;
        int profit = 0;

        for(int i=1;i< prices.length;i++) {

            // 如果当天买入的成本更低，那就证明当天更适合买入（同时也证明了当天绝不适合卖出）
            if ((prices[i]+fee)<buy) {
                buy = prices[i] + fee;
            } else if (prices[i]>buy) {
                // 如果当天卖出能赚钱，就立即卖出
                profit += prices[i] - buy;
                // 并且当天买入（题目要求一天不能同时买和卖，这里当天买卖，相当于减去股价又加上股价，等同于啥也没做）
                buy = prices[i];
            }
        }

        return profit;
    }





    public static void main(String[] args) {
        System.out.println(new L0714().maxProfit(new int[]{1, 3, 2, 8, 4, 9},2)); // 8
        System.out.println(new L0714().maxProfit(new int[]{1,3,7,5,10,3},3)); // 6
    }
}