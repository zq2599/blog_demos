package practice;

/**
 * @program: leetcode
 * @description: 快排
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0415 {

    public String addStrings(String num1, String num2) {
        // 异常处理
        if (null==num1 || "".equals(num1.trim())) {
            return num2;
        }

        if(null==num2 || "".equals(num2.trim())) {
            return num1;
        }

        StringBuilder sbud = new StringBuilder();
        char[] c1 = num1.toCharArray();
        char[] c2 = num2.toCharArray();

        int len1 = c1.length;
        int len2 = c2.length;

        // 按位处理的循环长度
        int maxLen = len1>len2 ? len1 : len2;

        int addVal;

        // 是否进位
        boolean carry = false;
        for (int i=0; i<maxLen; i++) {

            addVal = (i<len1 ? (c1[len1-i-1]-48) : 0)
                   + (i<len2 ? (c2[len2-i-1]-48) : 0);

            // 低位有进位，就要加一
            if (carry) {
                addVal++;
            }

            // 是否产生进位
            carry = addVal>9;

            sbud.append(addVal%10);
        }

        if(carry) {
            sbud.append("1");
        }

        return sbud.reverse().toString();
    }


    public static void main(String[] args) {
        System.out.println(new L0415().addStrings("456", "77"));
    }
}
