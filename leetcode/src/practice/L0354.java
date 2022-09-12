package practice;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0354 {
    /*
    public int maxEnvelopes(int[][] envelopes) {

        Arrays.sort(envelopes, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {

                if (o1[0]==o2[0]) {
                    // 第0位相等的时候，按照第1位从大到小排列
                    return o2[1]-o1[1];
                }

                // 总的来说，二维数组是按照第0位从小到大排列
                return o1[0]-o2[0];
            }
        });

        int[] nums = new int[envelopes.length];

        for (int i=0;i<envelopes.length;i++) {
            nums[i] = envelopes[i][1];
        }

        return lengthOfLIS(nums);
    }

    public int lengthOfLIS(int[] nums) {

        // 一个元素，代表一堆的最顶部
        int[] piles = new int[nums.length];
        piles[0] = nums[0];

        // 一共有几堆
        int pileNum = 1;

        // 左指针，右指针，中间指针
        int left, right, middle;

        // 处理每一个元素
        for (int i=1;i<nums.length;i++) {

            left = 0;
            right = pileNum;

            while(left<right) {
                middle = (left+right)/2;

                if (nums[i]<piles[middle]) {
                    right = middle;
                } else if (nums[i]>piles[middle]) {
                    left = middle+1;
                } else {
                    // 连续出现的数字，要移动右侧指针
                    // 不能移动左侧，因为左侧变大，会导致新增一个堆，答题结果会变大
                    right = middle;
                }
            }

            // 超出右边界的时候，表示数字过大，当前位置已经放不下了
            if (left==pileNum) {
                piles[pileNum++] = nums[i];
            } else {
                piles[left] = nums[i];
            }
        }

        return pileNum;
    }
    */


    public int maxEnvelopes(int[][] envelopes) {

        Arrays.sort(envelopes, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {

                if (o1[0]==o2[0]) {
                    // 第0位相等的时候，按照第1位从大到小排列
                    return o2[1]-o1[1];
                }

                // 总的来说，二维数组是按照第0位从小到大排列
                return o1[0]-o2[0];
            }
        });

        // 一个元素，代表一堆的最顶部
        int[] piles = new int[envelopes.length];
        piles[0] = envelopes[0][1];

        // 一共有几堆
        int pileNum = 1;

        // 左指针，右指针，中间指针
        int left, right, middle;

        // 处理每一个元素
        for (int i=1;i<envelopes.length;i++) {

            left = 0;
            right = pileNum;

            while(left<right) {
                middle = (left+right)/2;

                if (envelopes[i][1]<piles[middle]) {
                    right = middle;
                } else if (envelopes[i][1]>piles[middle]) {
                    left = middle+1;
                } else {
                    // 连续出现的数字，要移动右侧指针
                    // 不能移动左侧，因为左侧变大，会导致新增一个堆，答题结果会变大
                    right = middle;
                }
            }

            // 超出右边界的时候，表示数字过大，当前位置已经放不下了
            if (left==pileNum) {
                piles[pileNum++] = envelopes[i][1];
            } else {
                piles[left] = envelopes[i][1];
            }
        }

        return pileNum;
    }



    public static void main(String[] args) {
        System.out.println(new L0354().maxEnvelopes(new int[][]{{5,4},{6,4}, {6,7},{2,3}})); // 3
        System.out.println(new L0354().maxEnvelopes(new int[][]{{5,4},{5,5},{6,4}, {6,7},{2,3}})); // 3
        System.out.println(new L0354().maxEnvelopes(new int[][]{{1,1},{1,1}})); // 1
        System.out.println(new L0354().maxEnvelopes(new int[][]{{4,5},{4,6},{6,7},{2,3},{1,1},{1,1}})); // 4
        System.out.println(new L0354().maxEnvelopes(new int[][]{{46,89},{50,53},{52,68},{72,45},{77,81}})); // 3
    }
}
