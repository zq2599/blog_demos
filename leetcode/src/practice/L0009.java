package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0009 {

    public boolean isPalindrome(int x) {
        // 负数，10的整数倍，都不是回文数
        if ((x<0) || (0==x%10 && 0!=x)) {
            return false;
        }

        // 回文数右侧部分
        int right = 0;

        while(x>right) {
            // 123321 为例
            // 第一次循环 right=1, x=12332
            // 第二次循环 right=1*10 + 12332%10=12, x=1233
            // 第三次循环 right=12*10 + 1233%10=123

            // 1239321 为例
            // 第一次循环 right=1, x=123932
            // 第二次循环 right=1*10 + 123932%10=12, x=12393
            // 第三次循环 right=12*10 + 12393%10=123, x=1239
            // 第四次循环 right=123*10 + 1239%10=1239, x=123
            right = right*10 + x%10;
            x/=10;
        }

        // 如果x长度为双数，例如123321，此时x=right
        // 如果x长度为奇数，例如1239321，此时x=123，right=1239，也就是说中间那一位被放在了right末尾，需要去掉再比较
        return x==right || x==right/10;
    }





    public static void main( String[] args ) {
        System.out.println(new L0009().isPalindrome(12393211));
    }
}
