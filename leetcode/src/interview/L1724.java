package interview;

import practice.Tools;

import java.util.Arrays;

public class L1724 {

    /**
     * 计算最大子序和的起止位置和值
     * @param nums
     * @return 一共三个元素，每个元素的作用，0:lcs起始位置，1：lcs结束位置，2：子序列值
     */
    private int[] maxSubArray(int[] nums) {
        int[] rlt = new int[3];

        // dp[i]定义：选定num[i]时候的最大值
        int dp = nums[0];
        boolean[] isHead = new boolean[nums.length];
        isHead[0] = true;

        int maxVal = nums[0];
        int maxValIndex = 0;

        for (int i=1;i<nums.length;i++) {
            // 如果当前值加上前面的dp，比当前大，就不从头开始
            if ((dp+nums[i])>nums[i]) {
                dp = dp + nums[i];
            } else {
                dp = nums[i];
                // isHead[i]等于true，表示nums[i]是子串的起始位置，不连接前面的
                isHead[i] = true;
            }

            if (dp>maxVal) {
                maxVal = dp;
                maxValIndex = i;
            }
        }

        rlt[1] = maxValIndex;
        rlt[2] = maxVal;

        for (int i=maxValIndex;i>0;i--) {
            if (isHead[i]) {
                rlt[0] = i;
                break;
            }
        }

        return rlt;
    }

    public int[] getMaxMatrix(int[][] matrix) {
        int max = Integer.MIN_VALUE;
        int[] rlt = new int[4];
        int[] maxSubArrayRlt;
        int[] array = new int[matrix[0].length];

        for(int i=0;i< matrix.length;i++) {
            // 清零
            Arrays.fill(array, 0);

            for (int j=i;j< matrix.length;j++) {

                // 起始行是i，每次在上次的基础上累加一行
                for (int addIndex=0;addIndex< array.length;addIndex++) {
                    array[addIndex] += matrix[j][addIndex];
                }

                maxSubArrayRlt = maxSubArray(array);

                if (maxSubArrayRlt[2]>max) {
                    max = maxSubArrayRlt[2];
                    rlt[0] = i;
                    rlt[1] = maxSubArrayRlt[0];
                    rlt[2] = j;
                    rlt[3] = maxSubArrayRlt[1];
                }
            }
        }

        return rlt;
    }

    public static void main(String[] args) {


        int[][] array = new int[][]{
                {-1,0},
                {0,-1},
        };

        array = new int[][]{
                {-1, 3, -1},
                {2, -1, 3},
                {-3, 1, 2}};

        Tools.print(new L1724().getMaxMatrix(array)); // 0,1,2,2



    }
}
