package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0704 {

//    /**
//     * 二分查找，迭代
//     * @param nums
//     * @param target
//     * @return
//     */
//    public int search(int[] nums, int target) {
//        if (null==nums || nums.length<1) {
//            return -1;
//        }
//
//        return search(0, nums.length-1, nums, target);
//    }
//
//    public int search(int start, int end, int[] nums, int target) {
//        if(end<start) {
//            return -1;
//        }
//        int middle = start + (end-start)/2;
//
//        if (target>nums[middle]) {
//            // 目标值大于中间值，就搜索右侧
//            return search(middle+1, end, nums, target);
//        } else if (target<nums[middle]) {
//            // 目标值小鱼中间值，就所有左侧
//            return search(start, middle-1, nums, target);
//        }
//
//        return middle;
//    }

    public int search(int[] nums, int target) {
        if (null==nums || nums.length<1) {
            return -1;
        }

        int start =0;
        int end = nums.length-1;
        int middle;

        while(start<=end) {
            middle = start + (end-start)/2;

            if (target<nums[middle]) {
                end = middle -1;
            } else if (target>nums[middle]) {
                start = middle + 1;
            } else {
                return middle;
            }
        }

        return -1;
    }

    public static void main(String[] args) {
        System.out.println(new L0704().search(new int[] {-1,0,3,5,9,12}, 9));
    }
}
