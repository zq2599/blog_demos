package practice;

/**
 * @program: leetcode
 * @description: 快排
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0509 {


    /*
    public int fib(int n) {
        if(n<2){
            return n;
        }

        int[] array = new int[n+1];
        array[1] = 1;

        for(int i=2;i<=n;i++) {
            array[i] = array[i-1] + array[i-2];
        }

        return array[n];
    }
    */

    public int fib(int n) {
        if(n<2){
            return n;
        }

        int prePre = 0;
        int pre = 1;
        int cur = 0;

        for(int i=2;i<=n;i++) {
            // 算出当前值
            cur = prePre + pre;
            // 为下一个循环做准备，
            // pre成为prePre
            prePre = pre;
            // 当前值成为pre
            pre = cur;
        }

        return cur;
    }

    public static void main(String[] args) {
        System.out.println(new L0509().fib(4));
    }
}
