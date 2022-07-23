package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0338 {

//    public int[] countBits(int n) {
//        int[] bitNum = new int[n+1];
//
//        // 注意，循环要从1开始
//        for(int i=1;i<bitNum.length;i++) {
//            // 循环执行到i的时候，i&(i-1)一定已经执行过了
//            bitNum[i] = bitNum[i&(i-1)] + 1;
//        }
//
//        return bitNum;
//    }

    public int[] countBits(int n) {
        // N等于奇数，N的1的个数等于N-1的1的个数+1
        // N等于偶数，N的1的个数等于N/2的1的个数(注意，N/2可能是奇数，也可能是偶数)
        int[] bitNum = new int[n+1];

        // 注意，循环要从1开始
        for(int i=1;i<bitNum.length;i++) {
            // 用i&1即可区分奇偶
            bitNum[i] = 1==(i&1) ? (bitNum[i-1]+1) : bitNum[i>>1];
        }

        return bitNum;
    }



    public static void main(String[] args) {
        Tools.print(new L0338().countBits(2));
    }
}
