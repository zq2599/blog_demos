package offer;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0063 {

    /*
    public int maxProfit(int[] prices) {
        if (0==prices.length) {
            return 0;
        }

        int lastWithout = 0;
        int todayWithout = 0;
        int with = -prices[0];

        for (int i=1;i< prices.length;i++) {
            todayWithout = Math.max(lastWithout, with+prices[i]);
            // 注意，由于只能交易一次，所以持有股票时的最大利润，就是当天股价的负数
            with = Math.max(with, -prices[i]);
            lastWithout = todayWithout;
        }

        return Math.max(0, todayWithout);
    }
    */

    public int maxProfit(int[] prices) {
        // 最大利润
        int maxProfit = 0;
        // 最小购买价格
        int minPrice = Integer.MAX_VALUE;

        for (int i=0;i< prices.length;i++) {
            // 假设当前是卖出时机，检查赚的钱是否是最大（一定是和之前的最小购买价格相减）
            maxProfit = Math.max(maxProfit, prices[i] - minPrice);

            // 假设当前是买入时机，检查买入的成本是否更低
            minPrice = Math.min(minPrice, prices[i]);
        }

        return Math.max(0, maxProfit);
    }

    public static void main(String[] args) {
        System.out.println(new L0063().maxProfit(new int[]{7,1,5,3,6,4})); // 5
        System.out.println(new L0063().maxProfit(new int[]{7,6,4,3,1})); // 0
    }
}
