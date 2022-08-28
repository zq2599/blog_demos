package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0123 {

    // 0 : 第一次买入
    // 1 : 第一次卖出
    // 2 : 第二次买入
    // 3 : 第二次卖出

    /*
    public int maxProfit(int[] prices) {
        if (1==prices.length) {
            return 0;
        }

        int[][] dp = new int[prices.length][4];

        // 股票交易第一天结束
        // 0 : 第一次买入
        dp[0][0] = -prices[0];
        // 1 : 第一次卖出
        dp[0][1] = 0;
        // 2 : 第二次买入
        dp[0][2] = 0;
        // 3 : 第二次卖出
        dp[0][3] = 0;

        // 股票交易第二天结束
        // 0 : 第一次买入，前两天的最低股价
        dp[1][0] = Math.max(dp[0][0], -prices[1]);
        // 1 : 第一次卖出，只能是第0天买入，第1天卖出
        dp[1][1] = prices[1]-prices[0];
        // 2 : 第二次买入，股市总共才两天，不可能出现第二次买入
        dp[1][2] = 0;
        // 3 : 第二次卖出，股市总共才两天，不可能出现第二次卖出
        dp[1][3] = 0;

        // 股票交易第二天结束，只能有一次卖出
        if(2==prices.length) {
            return Math.max(0, dp[1][1]);
        }

        // 股票交易第三天结束
        // 0 : 第一次买入，两种可能：之前第一次买入，当天第一次买入(一分钱没有，再减去当天股价)
        dp[2][0] = Math.max(dp[1][0], -prices[2]);
        // 1 : 第一次卖出，两种可能：之前第一次卖出，当天第一次卖出(之前第一次买入，再加上当天股价)
        dp[2][1] = Math.max(dp[1][1], dp[1][0]+prices[2]);
        // 2 : 第二次买入，股票交易第三天结束，只可能是第一天买->第二天卖->第三天买
        dp[2][2] = -prices[0] + prices[1] - prices[2];
        // 3 : 第二次卖出，股票交易第三天结束，不可能出现第二次卖出
        dp[2][3] = 0;

        // 股票交易第三天结束，只能有一次卖出
        if(3==prices.length) {
            return Math.max(0, dp[2][1]);
        }

        // 股票交易第四天结束
        // 0 : 第一次买入，两种可能：之前第一次买入，当天第一次买入(一分钱没有，再减去当天股价)
        dp[3][0] = Math.max(dp[2][0], -prices[3]);
        // 1 : 第一次卖出，两种可能：之前第一次卖出，当天第一次卖出(之前第一次买入，再加上当天股价)
        dp[3][1] = Math.max(dp[2][1], dp[2][0]+prices[3]);
        // 2 : 第二次买入，两种可能：之前第二次买入，当天第二次买入(之前第一次卖出，再减去当天股价)
        dp[3][2] = Math.max(dp[2][2], dp[2][1]-prices[3]);
        // 3 : 第二次卖出，股票交易第四天结束，只可能是第一天买->第二天卖->第三天买->第四天卖
        dp[3][3] = -prices[0] + prices[1] - prices[2] + prices[3];

        // 股票交易第四天结束，一次或者两次卖出都有可能
        if(4==prices.length) {
            return Math.max(0, Math.max(dp[3][1], dp[3][3]));
        }

        // 注意审题，prices.length>=1

        for (int i=4;i<prices.length;i++) {
            // 0 : 第一次买入，两种可能：之前第一次买入，当天第一次买入(一分钱没有，再减去当天股价)
            dp[i][0] = Math.max(dp[i-1][0], -prices[i]);
            // 1 : 第一次卖出，两种可能：之前第一次卖出，当天第一次卖出(之前第一次买入，再加上当天股价)
            dp[i][1] = Math.max(dp[i-1][1], dp[i-1][0]+prices[i]);
            // 2 : 第二次买入，两种可能：之前第二次买入，当天第二次买入(之前第一次卖出，再减去当天股价)
            dp[i][2] = Math.max(dp[i-1][2], dp[i-1][1]-prices[i]);
            // 3 : 第二次卖出，两种可能：之前第二次卖出，当天第二次卖出(之前第二次买入，再加上当天股价)
            dp[i][3] = Math.max(dp[i-1][3], dp[i-1][2]+prices[i]);
        }

        // 注意，如果算出来是负数，就返回0，表示这些天未做交易
        return Math.max(0, Math.max(dp[prices.length-1][1], dp[prices.length-1][3]));
    }
    */

    /*
    public int maxProfit(int[] prices) {
        if (1==prices.length) {
            return 0;
        }

        int buy1 = 0;
        int sell1 = 0;
        int buy2 = 0;
        int sell2 = 0;

        // 股票交易第一天结束
        // 0 : 第一次买入
        buy1 = -prices[0];
        // 1 : 第一次卖出，不可能发生
        // 2 : 第二次买入，不可能发生
        // 3 : 第二次卖出，不可能发生

        // 股票交易第二天结束
        // 0 : 第一次买入，前两天的最低股价
        buy1 = Math.max(buy1, -prices[1]);
        // 1 : 第一次卖出，只能是第0天买入，第1天卖出
        sell1 = prices[1]-prices[0];
        // 2 : 第二次买入，股市总共才两天，不可能出现第二次买入
        // 3 : 第二次卖出，股市总共才两天，不可能出现第二次卖出

        // 股票交易第二天结束，只能有一次卖出
        if(2==prices.length) {
            return Math.max(0, sell1);
        }

        // 股票交易第三天结束
        // 0 : 第一次买入，两种可能：之前第一次买入，当天第一次买入(一分钱没有，再减去当天股价)
        buy1 = Math.max(buy1, -prices[2]);
        // 1 : 第一次卖出，两种可能：之前第一次卖出，当天第一次卖出(之前第一次买入，再加上当天股价)
        sell1 = Math.max(sell1, buy1+prices[2]);
        // 2 : 第二次买入，股票交易第三天结束，只可能是第一天买->第二天卖->第三天买
        buy2 = -prices[0] + prices[1] - prices[2];
        // 3 : 第二次卖出，股票交易第三天结束，不可能出现第二次卖出

        // 股票交易第三天结束，只能有一次卖出
        if(3==prices.length) {
            return Math.max(0, sell1);
        }

        // 股票交易第四天结束
        // 0 : 第一次买入，两种可能：之前第一次买入，当天第一次买入(一分钱没有，再减去当天股价)
        buy1 = Math.max(buy1, -prices[3]);
        // 1 : 第一次卖出，两种可能：之前第一次卖出，当天第一次卖出(之前第一次买入，再加上当天股价)
        sell1 = Math.max(sell1, buy1+prices[3]);
        // 2 : 第二次买入，两种可能：之前第二次买入，当天第二次买入(之前第一次卖出，再减去当天股价)
        buy2 = Math.max(buy2, sell1-prices[3]);
        // 3 : 第二次卖出，股票交易第四天结束，只可能是第一天买->第二天卖->第三天买->第四天卖
        sell2 = -prices[0] + prices[1] - prices[2] + prices[3];

        // 股票交易第四天结束，一次或者两次卖出都有可能
        if(4==prices.length) {
            return Math.max(0, Math.max(sell1, sell2));
        }

        // 注意审题，prices.length>=1

        for (int i=4;i<prices.length;i++) {
            // 0 : 第一次买入，两种可能：之前第一次买入，当天第一次买入(一分钱没有，再减去当天股价)
            buy1 = Math.max(buy1, -prices[i]);
            // 1 : 第一次卖出，两种可能：之前第一次卖出，当天第一次卖出(之前第一次买入，再加上当天股价)
            sell1 = Math.max(sell1, buy1+prices[i]);
            // 2 : 第二次买入，两种可能：之前第二次买入，当天第二次买入(之前第一次卖出，再减去当天股价)
            buy2 = Math.max(buy2, sell1-prices[i]);
            // 3 : 第二次卖出，两种可能：之前第二次卖出，当天第二次卖出(之前第二次买入，再加上当天股价)
            sell2 = Math.max(sell2, buy2+prices[i]);
        }

        // 注意，如果算出来是负数，就返回0，表示这些天未做交易
        return Math.max(0, Math.max(sell1, sell2));
    }
    */


    public int maxProfit(int[] prices) {
        if (1==prices.length) {
            return 0;
        }

        int buy1 = -prices[0];
        int sell1 = 0;
        // 注意，在计算过程中，如果buy2的默认值是0，当i小于4的时候，也就是第二次买卖还不存在的时候，让第二次买入状态的最大利润等于0，会影响后面sell2的计算，所以应该让其等于buy1才对
        // 此时buy2是不存在的，如果给buy2默认值是0，就会导致所以""
        int buy2 = -prices[0];
        int sell2 = 0;

        for (int i=1;i<prices.length;i++) {
            // 0 : 第一次买入，两种可能：之前第一次买入，当天第一次买入(一分钱没有，再减去当天股价)
            buy1 = Math.max(buy1, -prices[i]);
            // 1 : 第一次卖出，两种可能：之前第一次卖出，当天第一次卖出(之前第一次买入，再加上当天股价)
            sell1 = Math.max(sell1, buy1+prices[i]);
            // 2 : 第二次买入，两种可能：之前第二次买入，当天第二次买入(之前第一次卖出，再减去当天股价)
            buy2 = Math.max(buy2, sell1-prices[i]);
            // 3 : 第二次卖出，两种可能：之前第二次卖出，当天第二次卖出(之前第二次买入，再加上当天股价)
            sell2 = Math.max(sell2, buy2+prices[i]);
        }

        // 注意，如果算出来是负数，就返回0，表示这些天未做交易
        return Math.max(0, Math.max(sell1, sell2));
    }

    public static void main(String[] args) {
        System.out.println(new L0123().maxProfit(new int[]{3,3,5,0,0,3,1,4})); // 6
        System.out.println(new L0123().maxProfit(new int[]{1,2,3,4,5})); // 4
        System.out.println(new L0123().maxProfit(new int[]{7,6,4,3,1})); // 0
        System.out.println(new L0123().maxProfit(new int[]{1})); // 0
        System.out.println(new L0123().maxProfit(new int[]{2,1,4,5,2,9,7})); // 11
    }
}
