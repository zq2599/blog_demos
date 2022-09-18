package practice;

import java.util.Arrays;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0048 {

    /*
    public void rotate(int[][] matrix) {
        // 复制一份
        int[][] copy = new int[matrix.length][matrix.length];

        for (int i=0;i< matrix.length;i++) {
            for (int j=0;j<matrix.length;j++) {
                copy[i][j] = matrix[i][j];
            }
        }

        // 转换规律：from[row][col] to[col][n-row-1]
        for (int i=0;i< matrix.length;i++) {
            for (int j=0;j< matrix.length;j++) {
                matrix[j][matrix.length-i-1] = copy[i][j];
            }
        }
    }
    */
    public void rotate(int[][] matrix) {
        int temp;
        for (int i=0;i< matrix.length/2;i++) {
            for (int j=0;j<matrix.length;j++) {
                temp = matrix[i][j];
                matrix[i][j] = matrix[matrix.length-1-i][j];
                matrix[matrix.length-1-i][j] = temp;
            }
        }

        for (int i=0;i< matrix.length-1;i++) {
            for (int j=i+1;j<matrix.length;j++) {
                temp = matrix[i][j];
                matrix[i][j] = matrix[j][i];
                matrix[j][i] = temp;
            }
        }
    }





    public static void main(String[] args) {
//        int[][] array = new int[][]{{1,2,3},{4,5,6},{7,8,9}};
        int[][] array = new int[][]{{1,2},{3,4}};

        new L0048().rotate(array);

        Tools.print(array);
    }
}
