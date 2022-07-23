package practice;

import java.util.Arrays;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0001 {

    public void merge(int[] nums1, int m, int[] nums2, int n) {

        // 如果nums1和nums2同时为空，直接返回
        if (m+n==0) {
            return;
        }

        // 如果只有nums2为空，此时nums1就是最终结果，直接返回
        if (n==0) {
            return;
        }

        // 如果只有nums1为空，就要把nums2的内容复制到nums1中，作为合并结果
        if (m==0) {
            System.arraycopy(nums2, 0, nums1, 0, n);
            return;
        }

        // 记录数组nums1的比较位置，移动方向是从尾部到头部
        int offset1 = m-1;

        // 记录数组nums2的比较位置，移动方向是从尾部到头部
        int offset2 = n-1;

        // 一次处理一个数字，共m+n个数字，因此循环m+n次
        for (int i=m+n-1; i>=0; i--) {
            // 比较两个数组用于比较的位置的值，将大的数字放入num1的i位置
            if(nums1[offset1]>=nums2[offset2]) {
                // 如果nums2的值大，就将其放入nums1的i位置
                nums1[i] = nums1[offset1];

                // 注意指针的移动方向是从大到小
                offset1--;

                // 如果nums1已经全部移动完毕，那么比较工作就结束了，
                // 此时musm1[i]到[m+n-1]的内容就是所有nums1和部分mums2，然后，再加上nums2中没有参与合并的那一部分，即nums2[0]到[offset2]，这两段加在一起就是完整的合并数据
                // 所以，把nums2[0]到[offset2]复制到nums1的从头开始的位置即可
                if(offset1<0) {
                    System.arraycopy(nums2, 0, nums1, 0, offset2+1);
                    return;
                }
            } else {
                // 如果nums2的值大，就将其放入nums1的i位置
                nums1[i] = nums2[offset2];
                // 注意指针的移动方向是从大到小
                offset2--;

                // 如果nums2已经全部移入了nums1，那么比较工作就结束了，
                // nums1中没有参与比较的那一段(0-offset1)，就原封不动的放在nums1中即可，因为它们就是最小的那段
                if(offset2<0) {
                    return;
                }
            }
        }
    }




    public static void main( String[] args )
    {
        int[] nums1 = {0};
        int[] nums2 = {1};


        new L0001().merge(nums1, 0, nums2, 1);

        System.out.println(Arrays.toString(nums1) );
    }
}
