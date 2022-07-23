package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0461 {


    public int hammingDistance(int x, int y) {
        int val = x ^ y;

        int count = 0;

        while(val>0) {
            count++;
            val = val & (val-1);
        }

        return count;
    }


    public static void main( String[] args ) {
        System.out.println(new L0461().hammingDistance(93,73));
    }
}
