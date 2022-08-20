package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0089 {

    public List<Integer> grayCode(int n) {
        int[] currentArray = new int[(int)Math.pow(2, n)];
        // 保存前一次的结果，用一半大小即可
        int[] prevArray = new int[currentArray.length/2];

        for (int i=1;i<=n;i++) {

            int size = (int)Math.pow(2,i);

            // currentArray的前一半，就等于prevArray的内容
            // 然而，由于prevArray的内容是从currentArray中复制的(循环的上一次)，
            // 所以prevArray和currentArray内容是相等的，那么复制就没有必要了
            // System.arraycopy(prevArray,0, currentArray, 0,size/2);

            // 后一半，
            for(int j=size/2;j<size;j++) {
                // 千万注意，<<的优先级很低，要用括号
                currentArray[j] = prevArray[size-j-1] + (1<<(i-1));
            }

            // 只要不是最后一次循环就复制
            if (i!=n) {
                System.arraycopy(currentArray,0, prevArray, 0, size);
            }
        }

        List<Integer> list = new ArrayList<>();

        for(int i : currentArray) {
            list.add(i);
        }

        return list;
    }

    public static void main(String[] args) {
        System.out.println(new L0089().grayCode(3));
    }
}
