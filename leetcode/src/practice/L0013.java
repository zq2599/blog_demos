package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0013 {

    public int romanToInt(String s) {
        if (null==s || s.length()<1) {
            return 0;
        }

        int total = 0;

        // 这道题的核心是一个规则：如果小字母在左侧，就是负数
        char[] array = s.toCharArray();

        // 如果长度只有1，就提前返回了
        if (array.length<2) {
            return getValue(array[0]);
        }

        int current;
        int next = getValue(array[0]);
        // 遍历每个字符，计算总和，
        // 注意遍历方向，是从左到右，因为需要检查下一个字符才能知道当前字符是正还是负数，
        // 还有一处要注意，for循环要提前结束，因为每个当前值都要检查后一位才能确定正负
        for (int i=0;i<array.length-1;i++) {
            // 上一轮循环就计算过array[i+1]的值，这里无需再次计算
            current = next;
            next = getValue(array[i+1]);

            // 如果当期值比后面的小，就是负数
            if (current<next) {
                current *= -1;
            }

            total += current;
        }

        // 因为提前结束了循环，记得还要把最后一位加上
        return total + next;
    }



    private int getValue(char c) {
        int rlt;

        switch(c) {
            case 'I':
                rlt = 1;
                break;
            case 'V':
                rlt = 5;
                break;
            case 'X':
                rlt = 10;
                break;
            case 'L':
                rlt = 50;
                break;
            case 'C':
                rlt = 100;
                break;
            case 'D':
                rlt = 500;
                break;
            case 'M':
                rlt = 1000;
                break;
            default:
                rlt = 0;
        }

        return rlt;
    }


    public static void main(String[] args) {


        System.out.println(new L0013().romanToInt("III"));
        System.out.println(new L0013().romanToInt("IV"));
        System.out.println(new L0013().romanToInt("IX"));
        System.out.println(new L0013().romanToInt("LVIII"));
        System.out.println(new L0013().romanToInt("MCMXCIV"));
    }
}
