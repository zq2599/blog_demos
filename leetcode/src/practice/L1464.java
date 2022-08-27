package practice;

import java.util.Arrays;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L1464 {

    public int maxProduct(int[] nums) {
        int max = -1;
        int second = -1;
        Arrays.sort(nums);
        for(int i=0;i<nums.length;i++) {
            // 注意，这里要用大于等于，如此以来，最大值出现两次的时候，可以保证max和second都是最大值
            if (nums[i]>=max) {
                // 如果max已经赋值过，那么可以把曾经的max让给second
                if (max>-1) {
                    second = max;
                }

                max = nums[i];

            } else if (second<nums[i]) {
                // max看不上的数字，要给second试试
                second = nums[i];
            }
        }

        return (max-1)*(second-1);
    }

    public static void main(String[] args) {
        System.out.println(new L1464().maxProduct(new int[] {3,4,5,2})); // 12
        System.out.println(new L1464().maxProduct(new int[] {1,5,4,5})); // 16
        System.out.println(new L1464().maxProduct(new int[] {3,7})); // 12
        System.out.println(new L1464().maxProduct(new int[] {7,3})); // 12
        System.out.println(new L1464().maxProduct(new int[] {10,2,5,2})); // 36

    }
}
