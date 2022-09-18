package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0007 {


    public int reverse(int x) {
        char[] array;

        if (x>=0) {
            array = Integer.toString(x).toCharArray();
        } else {
            array = Integer.toString(x).substring(1).toCharArray();
        }

        if (1==array.length) {
            return x;
        }

        char temp;

        for (int i=0;i<array.length/2;i++) {
            temp = array[i];
            array[i] = array[array.length-1-i];
            array[array.length-1-i] = temp;
        }

        long l = Long.parseLong(new String(array));

        return l>Integer.MAX_VALUE ?
                0 :
                (x>0 ? (int)l : -(int)l);
    }

    public static void main(String[] args) {
//        System.out.println(new L0007().reverse(123));
//        System.out.println(new L0007().reverse(-123));
//        System.out.println(new L0007().reverse(120));
//        System.out.println(new L0007().reverse(0));
//        System.out.println(new L0007().reverse(1534236469));
//        System.out.println(new L0007().reverse(-2147483648));
//        System.out.println(new L0007().reverse(0));

        System.out.println(Integer.MAX_VALUE);
        System.out.println(Integer.MIN_VALUE);
    }
}
