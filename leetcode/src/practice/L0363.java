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
        int maxOfMergeLine;

        int[] baseLine = new int[matrix[0].length];

        for (int i=0;i<matrix.length;i++) {

            Arrays.fill(baseLine, 0);

            // 从第i行开始，每次累加一行到baseLine中
            for(int j=i;j<matrix.length;j++) {
                // 每一列累加
                for(int colIndex=0;colIndex<baseLine.length;colIndex++) {
                    baseLine[colIndex] += matrix[j][colIndex];
                }

                maxOfMergeLine = getMaxSubArray(baseLine, k);

                // 如果等于k，可以提前结束了
                if (k==maxOfMergeLine) {
                    return k;
                }

                max = Math.max(max, maxOfMergeLine);
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
        // 千万注意，将0放入是至关重要的一步!!!
        // 一旦reeSet.ceiling返回0，后面的代码就会将sum作为最大值
        treeSet.add(0);
        int rlt = Integer.MIN_VALUE;
        int sum = 0;

        for (int i=0;i< nums.length;i++) {
            sum += nums[i];

            // 特殊情况，从队列头部累加到某个位置的值正好等于k，那就是满足题目要求了，立即返回k即可
            if (sum==k) {
                return k;
            }

            // sum-k的值假设是3，那么ceiling返回的就是3，或者大于3的最近的key
            Integer key = treeSet.ceiling(sum-k);

            // 如果这个key存在，那就是treeSet中最接近3的，
            // 注意这个推断：key越接近3， sum-key越接近k（由于key大于或者等于3，所以sum-key不会超过k，满足题意要求）
            // 这个最好在纸上画画
            if (null!=key) {
                rlt = Math.max(rlt, sum-key);
            }

            // 加入treeSet种
            treeSet.add(sum);
        }

        return rlt;
    }


    public static void main(String[] args) {
        int[][] array = new int[][]{
                {1,0,1},
                {0,-2,3}
        };


//        System.out.println(new L0363().maxSumSubmatrix(array,2)); // 2

        array = new int[][]{
                {2,2,-1}
        };

//        System.out.println(new L0363().maxSumSubmatrix(array,3)); // 3

        array = new int[][]{
                {27,5,-20,-9,1,26,1,12,7,-4,8,7,-1,5,8},
                {16,28,8,3,16,28,-10,-7,-5,-13,7,9,20,-9,26},
                {24,-14,20,23,25,-16,-15,8,8,-6,-14,-6,12,-19,-13},
                {28,13,-17,20,-3,-18,12,5,1,25,25,-14,22,17,12},
                {7,29,-12,5,-5,26,-5,10,-5,24,-9,-19,20,0,18},
                {-7,-11,-8,12,19,18,-15,17,7,-1,-11,-10,-1,25,17},
                {-3,-20,-20,-7,14,-12,22,1,-9,11,14,-16,-5,-12,14},
                {-20,-4,-17,3,3,-18,22,-13,-1,16,-11,29,17,-2,22},
                {23,-15,24,26,28,-13,10,18,-6,29,27,-19,-19,-8,0},
                {5,9,23,11,-4,-20,18,29,-6,-4,-11,21,-6,24,12},
                {13,16,0,-20,22,21,26,-3,15,14,26,17,19,20,-5},
                {15,1,22,-6,1,-9,0,21,12,27,5,8,8,18,-1},
                {15,29,13,6,-11,7,-6,27,22,18,22,-3,-9,20,14},
                {26,-6,12,-10,0,26,10,1,11,-10,-16,-18,29,8,-8},
                {-19,14,15,18,-10,24,-9,-7,-19,-14,23,23,17,-5,6}
        };

        System.out.println(new L0363().maxSumSubmatrix(array,-100)); // -101
    }
}
