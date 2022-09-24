package book;

import practice.Tools;

import java.util.Arrays;
import java.util.TreeSet;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0003 {

    public void rotate(int[] nums, int k) {
        if (0==k || 1==nums.length) {
            return;
        }

        k %= nums.length;

        int[] array = Arrays.copyOf(nums, nums.length);
        System.arraycopy(array, nums.length-k, nums, 0, k);
        System.arraycopy(array, 0, nums, k, nums.length-k);
    }





    public static void main(String[] args) {
        int[] array = new int[]{1,2,3,4,5,6,7};
        new L0003().rotate(array, 3);
        Tools.print(array); // 5,6,7,1,2,3,4
        System.out.println("");

        array = new int[]{-1,-100,3,99};
        new L0003().rotate(array, 2);
        Tools.print(array); // 3,99,-1,-100
    }
}
