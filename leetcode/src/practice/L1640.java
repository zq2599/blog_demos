package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L1640 {



    public boolean canFormArray(int[] arr, int[][] pieces) {
        return false;
    }



    public static void main(String[] args) {
        System.out.println(new L1640().canFormArray(new int[]{15,88},new int[][]{{88},{15}})); // true
        System.out.println(new L1640().canFormArray(new int[]{49,18,16},new int[][]{{16,18,49}})); // false
        System.out.println(new L1640().canFormArray(new int[]{91,4,64,78},new int[][]{{78},{4,64},{91}})); // true
    }
}

