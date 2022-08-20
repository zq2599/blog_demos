package practice;

import java.util.ArrayList;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0306of2 {


    public boolean isAdditiveNumber(String num) {
        int n = num.length();

        // 累加数需要至少三个数字，且第三个数字一定大于等于前两个数字，但前两个数字长度没有关系，需要枚举
        // len1为首个数字的长度，最小为1， 最大不超过n / 2，超过了后，第三个数字长度就不够了
        for (int len1 = 1; len1 <= n / 2; len1++) {
            String n1 = num.substring(0, len1);

            // len2为第二个数字的长度，最小为1
            // len3为第三个数字长度，长度最小值为max(len1, len2)，这是形成累加数的初始条件
            for (int len2 = 1, len3 = n - len1 - len2; len3 >= len1 && len3 >= len2; len2++) {
                String n2 = num.substring(len1, len1 + len2);

                if (dfs(num.substring(len1 + len2), n1, n2)) {
                    return true;
                }

                // len2=1时，如果n2 == "0"，直接退出循环，不可能有len2>1的情况（出现前导0）
                if (n2.equals("0")) break;
            }

            // len1=1时，如果n1 == "0"，直接退出循环，不可能有len1>1的情况（出现前导0）
            if (n1.equals("0")) break;
        }
        return false;
    }

    // num为剩余的字符串，从中找到第三个数字，为了简便，使用的是substring进行切割
    // n1为第一个数字，n2为第二个数字
    private boolean dfs(String num, String n1, String n2) {
        // 剩余字符串为空返回true
        if (num.length() == 0) return true;

        // 剩余字符串长度小于n1或者n2的长度，返回false
        if (num.length() < n1.length() || num.length() < n2.length()) return false;

        // 求和
        String sum = add(n1, n2);

        // 剩余的字符串长度小于求和的结果长度，返回false
        if (num.length() < sum.length()) return false;

        // 字符串匹配
        for (int i = 0; i < sum.length(); i++) {
            if (num.charAt(i) != sum.charAt(i)) return false;
        }

        System.out.println("n1 [" + n1 + "], n2 [" + n2 + "], num [" + num + "]");

        // 切割掉第三个数字，n2作为第一个数字，sum作为第二个数字，进行递归
        return dfs(num.substring(sum.length()), n2, sum);
    }

    // 寻找第三个数字时不需要考虑前导0的问题，因为字符串加法最终不会形成前导0（除非字符串全0）
    private String add(String n1, String n2) {
        StringBuilder sb = new StringBuilder();
        int i = n1.length() - 1, j = n2.length() - 1;
        int upper = 0;
        while (i >= 0 || j >= 0) {
            int a = 0, b = 0;
            if (i >= 0) a = n1.charAt(i) - '0';
            if (j >= 0) b = n2.charAt(j) - '0';
            int sum = a + b + upper;
            upper = sum / 10;
            sb.append(sum % 10);
            i--;
            j--;
        }
        if (upper != 0) sb.append(upper);
        return sb.reverse().toString();
    }


    public static void main(String[] args) {
//        System.out.println((new L0306().isAdditiveNumber("112358")));
//        System.out.println((new L0306().isAdditiveNumber("199100199")));
//        System.out.println((new L0306().isAdditiveNumber("1023")));
        // 预期是true
//        System.out.println((new L0306().isAdditiveNumber("101")));

        // 预期 false
//        System.out.println((new L0306().isAdditiveNumber("1203")));

//        System.out.println((new L0306().isAdditiveNumber("198019823962")));
        System.out.println((new L0306of2().isAdditiveNumber("121474836472147483648")));



//        System.out.println("1234".substring(0,1));
//        System.out.println("1234".substring(1,2));
//        System.out.println("1234".substring(2,3));
    }

}
