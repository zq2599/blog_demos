package practice;

/**
 * @program: leetcode
 * @description:
 * @author: zq2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L1784 {

    
    public boolean checkOnesSegment(String s) {
        char[] array = s.toCharArray();

        if (1==array.length) {
            return true;
        }

        boolean isZeroAppear = false;

        for (int i = 1; i < array.length; i++) {
            // 一旦出现零，就把isZeroAppear设置为true
            if ('0'==array[i]) {
                isZeroAppear = true;
            } else {
                // 如果0已经出现过，再出现1的时候，就可以直接返回了
                if (isZeroAppear) {
                    return false;
                }
            }           
        }
        
        return true;
    }


    public static void main(String[] args) {
        System.out.println(new L1784().checkOnesSegment("1001")); // false
        System.out.println(new L1784().checkOnesSegment("110")); // true
        System.out.println("");
    }
}
