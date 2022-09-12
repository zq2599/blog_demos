package practice;

import java.util.Arrays;
import java.util.TreeSet;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0363 {

    public int maxSumSubmatrix(int[][] matrix, int k) {
        int max = Integer.MIN_VALUE;

        int[] baseLine = new int[matrix[0].length];

        for (int i=0;i<matrix.length;i++) {

            Arrays.fill(baseLine, 0);

            // 从第i行开始，每次累加一行到baseLine中
            for(int j=i;j<matrix.length;j++) {
                // 每一列累加
                for(int colIndex=0;colIndex<baseLine.length;colIndex++) {
                    baseLine[colIndex] += matrix[j][colIndex];
                }

                max = Math.max(max, getMaxSubArray(baseLine, k));
            }
        }

        return max;
    }


    /**
     * 一维数组，取最大连续子数组，而且值不能超过k
     * @param nums
     * @param k
     * @return
     */
    private int getMaxSubArray(int[] nums, int k) {
        TreeSet<Integer> treeSet = new TreeSet<>();

        int sum = 0;

        for (int i=0;i< nums.length;i++) {
            sum += nums[i];

            treeSet.ceiling(sum-k);



            treeSet.add(sum);
        }


        return 0;




    }


    public static void main(String[] args) {
        int[][] array = new int[][]{
                {1,0,1},
                {0,-2,3}
        };


        System.out.println(new L0363().maxSumSubmatrix(array,2)); // 2
    }
}
