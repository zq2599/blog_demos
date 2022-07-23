package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0121 {


    // 股价、余额、是否持股、买进、卖出
    /*
    public int maxProfit(int[] prices) {
        // 异常处理
        if (prices.length<2) {
            return 0;
        }

        // dp数组定义：这是个二维数组dp[i][j]
        // i表示第几天，j表示是否持股(0表示不持股，1表示持股)，dp[i][j]的值表示手里现金的数量
        // dp[1][0]表示第1天结束的时候，在不持股的情况下，手里现金的数量
        // dp[1][1]表示第1天结束的时候，在持股的情况下，手里现金的数量
        // 第1天不持股，只有两种可能，第0天持股第1天卖出，或者第0天不持股第1天也不持股
        // 第1天持股，只有两种可能，第0天持股第1天持股，或者第0天不持股第一天买进
        // 注意，买进是个非常特殊的操作，一旦买进，手里金额直接等于当天股价的负数，和之前没有任何关系
        int[][] dp = new int[prices.length][2];

        // 初始化
        // 第0天，不买，手里现金为0
        dp[0][0] = 0;
        // 第0天，买，手里现金为负数
        dp[0][1] = -prices[0];

        for (int i=1;i<prices.length;i++) {
            // 第i天不持股的两种可能：i-1天也不持股第i天啥事不做，或者i-1天持股第i天卖出，
            // i-1天不持股，最大金额是：dp[i-1][0]
            // i-1天持股，第i天卖出，最大金额是：dp[i-1][1]+price[i]
            dp[i][0] = Math.max(dp[i-1][0], dp[i-1][1]+prices[i]);
            // 第i天持股的两种可能：i-1天也持股第i天啥事不做，或者i-1天不持股第i天买入
            dp[i][1] = Math.max(dp[i-1][1], -prices[i]);
        }

        // 到最后一天，不持股的情况下，手里的最大现金数量
        return dp[prices.length-1][0];
    }
    */

    public int maxProfit(int[] prices) {
        // 异常处理
        if (prices.length<2) {
            return 0;
        }

        // dp数组定义：这是个二维数组dp[i][j]
        // i表示第几天，j表示是否持股(0表示不持股，1表示持股)，dp[i][j]的值表示手里现金的数量
        // dp[1][0]表示第1天结束的时候，在不持股的情况下，手里现金的数量
        // dp[1][1]表示第1天结束的时候，在持股的情况下，手里现金的数量
        // 第1天不持股，只有两种可能，第0天持股第1天卖出，或者第0天不持股第1天也不持股
        // 第1天持股，只有两种可能，第0天持股第1天持股，或者第0天不持股第一天买进
        // 注意，买进是个非常特殊的操作，一旦买进，手里金额直接等于当天股价的负数，和之前没有任何关系

        // 初始化
        // 第0天，不持股，手里金额为0
        int prevWithoutStock = 0;
        // 第0天，持股，购买股票后手里金额为负数
        int prevWithStock = -prices[0];

        int curWithoutStock =0;
        int curWithStock;

        for (int i=1;i<prices.length;i++) {
            // 第i天不持股的两种可能：i-1天也不持股第i天啥事不做，或者i-1天持股第i天卖出，
            curWithoutStock = Math.max(prevWithoutStock, prevWithStock + prices[i]);
            // 第i天持股的两种可能：i-1天也持股第i天啥事不做，或者i-1天不持股第i天买入
            curWithStock = Math.max(prevWithStock, -prices[i]);

            prevWithoutStock = curWithoutStock;
            prevWithStock = curWithStock;
        }

        // 到最后一天，不持股的情况下，手里的最大现金数量
        return curWithoutStock;
    }

    public static void main( String[] args ) {
        System.out.println(new L0121().maxProfit(new int[] {7,1,5,3,6,4}));
    }
}
