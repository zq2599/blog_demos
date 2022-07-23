package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0026 {

    public int removeDuplicates(int[] nums) {
        // 处理异常
        if (nums.length<2) {
            return nums.length;
        }

        // 这道题的关键，就是升序排列，这样保证了同样数字只会连续出现，不会分散出现
        // 双指针解法：第一个可以写入的位置，正在处理的位置

        // 第一个可以写入的位置
        int firstWritablePos = -1;

        for (int i=1;i<nums.length;i++) {
            // firstWritablePos小于0表示还没有出现过重复数字，因此没有空洞
            if(firstWritablePos<0) {
                // 首次出现空洞，就是当前位置
                if(nums[i]==nums[i-1]){
                    firstWritablePos = i;
                }
            } else {
                // 如果有了空洞，因为是紧密排列的，因此有效数字是空洞前面那个，
                // 如果当前数字和空洞前面那个数字不等，就要移动当前数字到空洞位置，再将空洞指针向后移动一位
                if (nums[i]!=nums[firstWritablePos-1]) {
                    nums[firstWritablePos++] = nums[i];
                }
            }
        }

        return firstWritablePos<0 ? nums.length : firstWritablePos;
    }


    public static void main( String[] args ) {
        System.out.println(new L0026().removeDuplicates(new int[] {1,2}));
    }
}
