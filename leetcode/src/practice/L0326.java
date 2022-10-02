package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0326 {

    public boolean isPowerOfThree(int n) {
        /*
        if (n<1) {
            return false;
        }

        while(n%3==0) {
            n /= 3;
        }

        return 1==n;
         */    
        return n>0 && 1162261467%n==0;
    }

    public static void main(String[] args) {
        


        System.out.println(new L0326().isPowerOfThree(27)); // true
        System.out.println(new L0326().isPowerOfThree(0)); // false
        System.out.println(new L0326().isPowerOfThree(9)); // true
        System.out.println(new L0326().isPowerOfThree(45)); // false
        System.out.println("");
    }
}
