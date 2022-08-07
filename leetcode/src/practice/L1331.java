package practice;

import java.util.*;

/**
 * @program: leetcode
 * @description: 快排
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L1331 {

    /**
     * 列表大小等于或小于该大小，将优先于 quickSort 使用插入排序
     */
    private static final int INSERTION_SORT_THRESHOLD = 7;

    private static final java.util.Random RANDOM = new java.util.Random();

    /**
     * 对数组 nums 的子区间 [left, right] 使用插入排序
     *
     * @param nums  给定数组
     * @param left  左边界，能取到
     * @param right 右边界，能取到
     */
    private void insertionSort(int[] nums, int left, int right) {
        for (int i = left + 1; i <= right; i++) {
            int temp = nums[i];
            int j = i;
            while (j > left && nums[j - 1] > temp) {
                nums[j] = nums[j - 1];
                j--;
            }
            nums[j] = temp;
        }
    }

    private void swap(int aOffset, int bOffset, int[] nums) {
        int temp = nums[aOffset];
        nums[aOffset] = nums[bOffset];
        nums[bOffset] = temp;
    }

    private void quickSort(int left, int right, int[] nums) {
        if(left>=right) {
            return;
        }

        if((right-left)<INSERTION_SORT_THRESHOLD) {
           insertionSort(nums, left, right);
           return;
        }

        int randomIndex = left + RANDOM.nextInt(right - left + 1);
        swap(randomIndex, left, nums);

        int pivotLeft = left;
        int pivotRight = right+1;

        // 基准数取值
        int pivot = nums[left];

        while(true) {

            while (pivotLeft<right && nums[++pivotLeft]<pivot) {}


            // 注意，这里必须用left，千万不能用pivotLeft，
            // 假设只有两个值[2,3]，如果用pivotLeft，
            // 此时pivotLeft已经在前面的步骤中从0变成了1，
            // 那么pivotRight就没有机会自减了，
            // 那么在循环外面的swap，就是swap(0,1,nums)，
            // 那么[2,3]就变成了[3,2]
            while (pivotRight>left && nums[--pivotRight]>pivot) {}

            // 如果左右指针已经碰头，就在此退出循环
            if (pivotLeft>=pivotRight) {
                break;
            }

            // 把基准值放在中间
            swap(pivotLeft, pivotRight, nums);
        }

        // 注意，一定要用pivotRight，
        // 像[2,3]这样的数组，pivotRight在此等于0，pivotLeft在此等于1，
        // 一旦用pivotLeft，下面的操作就会把[2,3]变成[3,2]
        swap(left, pivotRight, nums);

        // 基准数位置左边的数组做一次快排
        quickSort(left, pivotRight-1, nums);
        // 基准数位置右边的数组做一次快排
        quickSort(pivotRight+1, right, nums);
    }

    /**
     * 快排入口
     * @param nums
     * @return
     */
    private int[] sortArray(int[] nums) {
        // 异常处理
        if(null==nums || nums.length<1) {
            return nums;
        }

        quickSort(0, nums.length-1, nums);
        return nums;
    }

    /*
    public int[] arrayRankTransform(int[] arr) {
        int[] raw = new int[arr.length];

        System.arraycopy(arr, 0, raw, 0, arr.length);

        int[] sorted = sortArray(arr);

        int lastIndex = 0;
        int lastValue = Integer.MIN_VALUE;

        Map<Integer,Integer> map = new HashMap<>();

        for (int i=0;i<sorted.length;i++) {
            if (sorted[i]==lastValue) {
                lastValue = sorted[i];
            } else {
                lastValue = sorted[i];
                // 索引增加
                lastIndex++;
            }

            map.put(lastValue, lastIndex);
        }

        for(int i=0;i<raw.length;i++) {
            sorted[i] = map.get(raw[i]);
        }

        return sorted;
    }
    */

    public int[] arrayRankTransform(int[] arr) {
        int[] sorted = Arrays.copyOf(arr, arr.length);
        Arrays.sort(sorted);

        int lastIndex = 0;
        int lastValue = Integer.MIN_VALUE;

        Map<Integer,Integer> map = new HashMap<>(arr.length);

        for (int i=0;i<sorted.length;i++) {
            if (sorted[i]==lastValue) {
                lastValue = sorted[i];
            } else {
                lastValue = sorted[i];
                // 索引增加
                lastIndex++;
            }

            map.put(lastValue, lastIndex);
        }

        for(int i=0;i<arr.length;i++) {
            arr[i] = map.get(arr[i]);
        }

        return arr;
    }

    public static void main(String[] args) {
        Tools.print(new L1331().arrayRankTransform(new int[] {40,10,20,30}));
        System.out.println("");
        Tools.print(new L1331().arrayRankTransform(new int[] {100,100,100}));
        System.out.println("");
        Tools.print(new L1331().arrayRankTransform(new int[] {37,12,28,9,100,56,80,5,12}));
    }
}
