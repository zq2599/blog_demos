package practice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L1636 {


    public int[] frequencySort(int[] nums) {
        // array[i]的定义：一共六位，前三位表示数量，后三位表示数字，为了让数字越大排在越前面，将数字N变成100-N
        int[] array  = new int[201];
        int offset;
        int value;
        // 表示有多少个独立数字
        int num = 0;

        for (int i=0;i<nums.length;i++) {

            offset = nums[i] + 100;

            value = array[offset];

            if (0==value) {
                value = 1000 + (100-nums[i]);
                num++;
            } else {
                value += 1000;
            }

            array[offset] = value;
        }


        int[] sortArray = new int[num];

        // num清零，用于sortArray的计数
        num = 0;

        for (int i=0;i< array.length;i++) {
            if (array[i]!=0) {
                sortArray[num++] = array[i];
            }

            if (num== sortArray.length) {
                break;
            }
        }

        Arrays.sort(sortArray);

        int times;

        List<Integer> list = new ArrayList<>();

        for (int i=0;i<sortArray.length;i++) {
            times = sortArray[i]/1000;
            value = 100 - sortArray[i]%1000;
            for(int j=0;j<times;j++) {
                list.add(value);
            }
        }

        int[] rlt = new int[list.size()];

        for (int i=0;i<rlt.length;i++) {
            rlt[i] = list.get(i);
        }

        return rlt;
    }



    public static void main(String[] args) {
        Tools.print(new L1636().frequencySort(new int[]{1,1,2,2,2,3})); // 3,1,1,2,2,2
        System.out.println("");
        Tools.print(new L1636().frequencySort(new int[]{2,3,1,3,2})); // 1,3,3,2,2
        System.out.println("");
        Tools.print(new L1636().frequencySort(new int[]{-1,1,-6,4,5,-6,1,4,1})); // 5,-1,4,4,-6,-6,1,1,1
    }
}
