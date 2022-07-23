package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0004 {

    public double findMedianSortedArrays(int[] nums1, int[] nums2) {

        // 异常情况，nums1和nums2同时无效
        if (nums1.length<1 && nums2.length<1) {
            return 0.0;
        }

        int totalLen = nums1.length + nums2.length;

        // 按照题目，如果是奇数，就寻找中间数，如果是偶数，就找出中间两个求平均值
        if (totalLen%2>0) {
           // 例如，总共有13个数字，这里的中间数就是第7个，注意，这里是从1开始算，这是第K小数字的习惯说法
           return findK(nums1, nums2, totalLen/2 + 1);
        } else {
           // 例如总共有4个数字，这里的中间数就是第2个和第3个之和除以二，注意这里的"第2"和"第3"都是从1开始算，
           // 注意，除数要用double型，否则会用int计算
           return (findK(nums1, nums2, totalLen/2) + findK(nums1, nums2, totalLen/2 + 1))/2.0;
        }
    }

    /**
     * 注意通常说法：寻找第K小的数字，这个K是从1开始算的！！！
     * @param nums1
     * @param nums2
     * @param k
     * @return
     */
    private int findK(int[] nums1, int[] nums2, int k) {

        // 数组1开始查找的起点
        int begin1 = 0;

        // 数组2开始查找的起点
        int begin2 = 0;

        while (true) {
            // 边界情况，数组下标等于数组长度的时候，说明已经越界了
            if (begin1== nums1.length) {
                // nums1找完了，就只能在nums2找剩余的元素了
                return nums2[begin2 + k -1];
            }

            if (begin2== nums2.length) {
                // nums2找完了，就只能在nums1找剩余的元素了
                return nums1[begin1 + k -1];
            }

            if (1==k) {
                return Math.min(nums1[begin1], nums2[begin2]);
            }

            int half  = k/2;
            // 正常情况
            // 注意要看是否数组越界，如果越界了，就只能返回末尾元素
            int newBegin1 = Math.min(begin1 + half, nums1.length) - 1;
            int newBegin2 = Math.min(begin2 + half, nums2.length) - 1;

            if(nums1[newBegin1]<nums2[newBegin2]) {
                // nums1本次用于比较的元素，全部都不再参与下一次比较，
                // 假设本次nums1有两个元素比较了，那么下一次寻找的新数组就少了两个元素，
                // 而且少的都是前面的，原本寻找第k个，变成了寻找第k-2个
                k = k - (newBegin1-begin1) - 1;
                begin1 = newBegin1 + 1;
            } else {
                k = k - (newBegin2-begin2) - 1;
                begin2 = newBegin2 + 1;
            }
        }
    }


    public static void main(String[] args) {
        System.out.println(new L0004().findMedianSortedArrays(
                new int[] {1,2},
                new int[] {3,4}));
    }
}
