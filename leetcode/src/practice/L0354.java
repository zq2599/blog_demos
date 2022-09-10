package practice;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0354 {

    public int maxEnvelopes(int[][] envelopes) {

        Arrays.sort(envelopes, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {

                if (o1[0]==o2[0]) {
                    return o1[1]-o2[1];
                }

                return o1[0]-o2[0];
            }
        });

        // dp[i]:一定使用第i个信封的时候，能套入的最大数量
        int dp[] = new int[envelopes.length];
        dp[0] = 1;
        int maxOfAll = 1;

        for (int i=1;i<envelopes.length;i++) {
            int max = 0;
            for (int j=i-1;j>=0;j--) {
                if (envelopes[i][0]>envelopes[j][0] && envelopes[i][1]>envelopes[j][1]) {

                    if (dp[j]>max) {
                        max = dp[j];
                    }
                }
            }

            dp[i] = max+1;

            if (dp[i]>maxOfAll) {
                maxOfAll = dp[i];
            }

        }

        return maxOfAll;
    }

    public static void main(String[] args) {
        System.out.println(new L0354().maxEnvelopes(new int[][]{{5,4},{6,4}, {6,7},{2,3}})); // 3
        System.out.println(new L0354().maxEnvelopes(new int[][]{{1,1},{1,1}})); // 1
        System.out.println(new L0354().maxEnvelopes(new int[][]{{4,5},{4,6},{6,7},{2,3},{1,1},{1,1}})); // 4
        System.out.println(new L0354().maxEnvelopes(new int[][]{{46,89},{50,53},{52,68},{72,45},{77,81}})); // 3
    }
}
