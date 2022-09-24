package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0008 {

    private static final int MAX_VAL_LEN = 10;

    public int myAtoi(String s) {
        char[] array = s.toCharArray();

        // 最大值是2147483647，最小值是-2147483648，它们的长度都不会超过十位
        char[] numberArray = new char[MAX_VAL_LEN];
        int numLen = 0;
        boolean isStartCollectNumber = false;
        // 默认是正数
        boolean isPositive = true;

        for (int i=0;i<array.length;i++) {
            // 还没有正式开始搜集数字
            if (!isStartCollectNumber) {
                // 一旦遇到数字，就开始搜集
                if (array[i]>='0' && array[i]<='9') {
                    isStartCollectNumber = true;

                    // 第一个数字，如果是0就不要
                    if ('0'!=array[i]) {
                        numberArray[numLen++] = array[i];
                    }
                } else if ('-'==array[i]) {
                    isPositive = false;
                    // 转入搜集数字模式
                    isStartCollectNumber = true;
                } else if ('+'==array[i]) {
                    // 转入搜集数字模式
                    isStartCollectNumber = true;
                } else if (' '==array[i]) {

                } else {
                    // 正负号、空格、数字之外的字符，就会结束读取
                    return 0;
                }
            } else {
                // 如果进入了搜集数字的模式
                if (array[i]>='0' && array[i]<='9') {

                    // 第一个数字，如果是0就不要
                    if ('0'!=array[i] || numLen>0) {
                        // 最大数字的长度是10，超过了就提前返回
                        if (MAX_VAL_LEN==numLen) {
                            return isPositive ? Integer.MAX_VALUE : Integer.MIN_VALUE;
                        }

                        numberArray[numLen++] = array[i];
                    }
                } else {
                   // 在搜集数字的模式下，用到非数字，就完成了数字的搜集
                   break;
                }
            }
        }

        int val = 0;
        boolean isMatchMax = false;
        int currentVal;

        for (int i=0;i<numLen;i++) {
            // 当前位的字符对应的数字
            currentVal = numberArray[i]-'0';

            // 判断超过整数范围的基本操作
            // 这里是处理倒数第一位
            if (MAX_VAL_LEN==numLen && i==(MAX_VAL_LEN-1)) {
                // 如果前面九位等于214748364，就要注意最后一位是否超出了
                if (isMatchMax) {

                    if (isPositive) {
                        if (currentVal>=7) {
                            return Integer.MAX_VALUE;
                        }
                    } else {
                        // 整个计算逻辑是先算出正数，再乘以-1，
                        // 所以对于-2147483648，对应的整数是放不下的（正数最大是2147483647），
                        // 所以在此直接返回吧
                        if (currentVal>=8) {
                            return Integer.MIN_VALUE;
                        }
                    }
                }
            }

            // 得到前i位对应的整数
            val = val*10 + currentVal;

            // 判断超过整数范围的基本操作，这里是处理倒数第二位
            if (MAX_VAL_LEN==numLen && i==(MAX_VAL_LEN-2)) {
                // 总长度是10的话，前面九位就不能超过214748364，一旦超过，就以为整个数字超过最大长度了，此时按题目要求返回整型最大值（负数就是最小值）
                if (214748364<val) {
                    return isPositive ? Integer.MAX_VALUE : Integer.MIN_VALUE;
                } else if (214748364==val) {
                    isMatchMax = true;
                }
            }
        }

        return isPositive ? val : -val;
    }


    public static void main(String[] args) {
        System.out.println(new L0008().myAtoi("42")); // 42
        System.out.println(new L0008().myAtoi("   -42")); // -42
        System.out.println(new L0008().myAtoi("4193 with words")); // 4193
        System.out.println(new L0008().myAtoi("words and 987")); // 0
        System.out.println(new L0008().myAtoi("+-12")); // 0
    }
}
