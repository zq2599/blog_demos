package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0122 {

    public int maxProfit(int[] prices) {
        return 0;
    }

    // dp[i][0] : 第i天结束后，手里有股票，最大价值
    // dp[i][1] : 第i天结束后，手里没有股票，最大价值


    public static void main(String[] args) {
        System.out.println(new L0122().maxProfit(new int[]{7,1,5,3,6,4})); // 7
        System.out.println(new L0122().maxProfit(new int[]{1,2,3,4,5})); // 4
        System.out.println(new L0122().maxProfit(new int[]{7,6,4,3,1})); // 0
    }
}
