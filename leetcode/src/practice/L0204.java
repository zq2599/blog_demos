package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0204 {

    public int countPrimes(int n) {

        // 根据质数定义,0和1都不是质数
        if (n < 2) {
            return 0;
        }

        int num = 0;

        byte[] flags = new byte[n];

        // 先假定都是质数
        for (int i = 2; i < flags.length; i++) {
            flags[i] = 1;
        }

        for (int i = 2; i * i < n; i++) {

            if (1 == flags[i]) {

                for (int j = i * i; j < n; j += i) {
                    flags[j] = 0;
                }
            }
        }

        for (int i = 2; i < flags.length; i++) {
            num += flags[i];
        }

        return num;
    }

    public static void main(String[] args) {
        System.out.println(new L0204().countPrimes(10)); // 4
        System.out.println(new L0204().countPrimes(0)); // 0
        System.out.println(new L0204().countPrimes(1)); // 0
        System.out.println(new L0204().countPrimes(2)); // 0

        System.out.println("");
    }
}
