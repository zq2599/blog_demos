package offer;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L10_1 {


    public int fib(int n) {
        if (n<2) {
            return n;
        }

        int lastLast = 0;
        int last = 1;
        int current = 0;

        for (int i=2;i<=n;i++) {
            current = (lastLast + last) % 1000000007 ;
            lastLast = last;
            // 千万注意，不要在结束的地方取模，而是在每次计算中
            last = current ;
        }

        return current ;
    }


    public static void main(String[] args) {
        System.out.println(new L10_1().fib(2));
        System.out.println(new L10_1().fib(5));
        System.out.println(new L10_1().fib(45));
        System.out.println(new L10_1().fib(48));
    }
}
