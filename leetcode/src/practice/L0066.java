package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0066 {

    public int[] plusOne(int[] digits) {

        // 代码走到这里，一定是进位了
        for(int i=0;i<digits.length;i++) {
            if (9==digits[digits.length-i-1]){
                digits[digits.length-i-1] = 0;
            } else {
                digits[digits.length-i-1]++;
                return digits;
            }
        }

        // 能走到这里来，证明每一位都进位了，那只有一种可能：原数组全是9
        int[] array = new int[digits.length+1];
        array[0] = 1;

        return array;
    }


    public static void main( String[] args ) {
        Tools.print(new L0066().plusOne(new int[] {1,2}));
    }
}
