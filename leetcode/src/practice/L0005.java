package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0005 {

    public String longestPalindrome(String s) {
        // 异常的特殊情况处理
        if(null==s || s.length()<2) {
            return s;
        }

        char[] array = s.toCharArray();

        boolean[][] dp = new boolean[array.length][array.length];

        int startOfMax = 0;
        int lenOfMax = 1;


        for (int j=1;j<array.length;j++) {
            for (int i=0;i<j;i++) {
                if (array[i]==array[j]
                && ((j-i)<3 || (dp[i+1][j-1]))) {
                    dp[i][j] = true;

                    if((j-i+1)>lenOfMax) {
                        startOfMax = i;
                        lenOfMax = j-i+1;
                    }

                }


                /*
                // 不满足回文的条件
                if(array[i]!=array[j]) {
                    dp[i][j] = false;
                    continue;
                }

                // 下面的逻辑，前提是array[i]==array[j]已经相等
                // 只有三个或者两个数字的情况，一定是回文串
                if ((j-i)<3) {
                    dp[i][j] = true;
                } else {
                    // 画个图看一下即可明白，此刻dp[i+1][j-1]已经在上一轮j的循环中计算出来了，
                    // 不用担心此处的i+1还没有计算
                    dp[i][j] = dp[i+1][j-1];
                }

                // 如果当期字符串是回文字符串，那么就要检查是否比已有记录更长
                if(dp[i][j]) {
                    if((j-i+1)>lenOfMax) {
                        startOfMax = i;
                        lenOfMax = j-i+1;
                    }
                }
                */
            }
        }

        return lenOfMax<1 ? "" : s.substring(startOfMax, startOfMax+lenOfMax);
    }



    public static void main(String[] args) {
        System.out.println(new L0005().longestPalindrome("ac"));
        System.out.println(new L0005().longestPalindrome("babad"));
        System.out.println(new L0005().longestPalindrome("cbbd"));
    }
}
