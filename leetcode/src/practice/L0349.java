package practice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0349 {

    /*
    public int[] intersection(int[] nums1, int[] nums2) {
        int[] flags  = new int[1001];
        List<Integer> list = new ArrayList<>();

        for (int i : nums1) {
            flags[i] = 1;
        }

        for(int i : nums2) {
            // 假设i=10，flag[10]=1表示nums2中第一次发现10
            if (1==flags[i]) {
                list.add(i);
                // 等于2之后，下次nums2中再发现10这个数字，也不会加入到list中了
                flags[i] = 2;
            }
        }

        int[] rlt = new int[list.size()];

        for (int i=0;i<rlt.length;i++) {
            rlt[i] = list.get(i);
        }

        return rlt;
    }
    */

    public int[] intersection(int[] nums1, int[] nums2) {
        byte[] flags  = new byte[1001];
        int len = 0;

        for (int i : nums1) {
            flags[i] = 1;
        }

        for(int i : nums2) {
            // 假设i=10，flag[10]=1表示nums2中第一次发现10
            if (1==flags[i]) {
                nums1[len++] = i;
                // 等于2之后，下次nums2中再发现10这个数字，也不会加入到list中了
                flags[i] = 2;
            }
        }

        int[] rlt = new int[len];

        for (int i=0;i<len;i++) {
            rlt[i] = nums1[i];
        }

        return rlt;
    }










    public static void main(String[] args) {
        Tools.print(new L0349().intersection(new int[]{1,2,2,1}, new int[]{2,2})); // 2
        System.out.println("");
        Tools.print(new L0349().intersection(new int[]{4,9,5}, new int[]{9,4,9,8,4})); // 9,4
    }
}
