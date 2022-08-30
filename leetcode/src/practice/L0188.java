package practice;

import java.util.Arrays;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0188 {

    public int maxProfit(int k, int[] prices) {
        if (prices.length<2) {
            return 0;
        }


        int days = prices.length;
        int buyTimes = Math.min(k, days/2);


        // buy[i][j] : 第i天股市结束后，处于第j次买入状态时，手里的最大利润
        // sell[i][j] : 第i天股市结束后，处于第j次卖出状态时，手里的最大利润
        int[][] buy = new int[days][buyTimes];
        int[][] sell = new int[days][buyTimes];

        Arrays.fill(buy[0], 0, buyTimes, -prices[0]);

        int max = 0;

        for (int i=1;i<days;i++) {
            for (int j=0;j< buyTimes;j++) {

                // 二选一：啥也不做，或者前一天的第j-1次卖出后，当天买入
                // 注意，j等于0表示初次买入，这时候不存在sell[i-1][j-1]，要特殊处理
                if (0==j) {
                    buy[i][j] = Math.max(buy[i-1][j], -prices[i]);
                } else {
                    buy[i][j] = Math.max(buy[i-1][j], sell[i-1][j-1]-prices[i]);
                }


                // 二选一：啥也不做，或者前一天的第j次买入后，当天卖出
                sell[i][j] = Math.max(sell[i-1][j], buy[i-1][j]+prices[i]);


                if (sell[i][j]>max) {
                    max = sell[i][j];
                }
            }
        }

        // 注意，如果算出来是负数，就返回0，表示这些天未做交易
        return max;
    }



    public static void main(String[] args) {
        System.out.println(new L0188().maxProfit(2, new int[]{2,4,1})); // 2
        System.out.println(new L0188().maxProfit(2, new int[]{3,2,6,5,0,3})); // 7
        System.out.println(new L0188().maxProfit(4, new int[]{1,2,4,2,5,7,2,4,9,0})); // 15
    }
}