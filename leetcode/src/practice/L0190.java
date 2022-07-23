package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0190 {

    private void print(int n) {
        for(int i=31;i>=0;i--) {
            System.out.print(   (n & (1<<i)) ==0 ? 0 : 1);
        }
        System.out.print("\n");
    }

    public int reverseBits(int n) {
        int val = 0;

        // 就像扑克牌，从底下抽出一张，放在新的位置的最前面，后面再抽的都接在尾部继续放
        for (int i=0;i<32;i++) {
          val = val<<1 | (n&1);
          n >>= 1;
        }

        return val;
    }


    public static void main(String[] args) {
        System.out.println(new L0190().reverseBits(-3));
    }
}
