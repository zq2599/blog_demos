package practice;

/**
 * @program: leetcode
 * @description: 快排
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0169 {

    // 注意这道题的前提：你可以假设数组是非空的，并且给定的数组总是存在多数元素
    public int majorityElement(int[] nums) {

        int defendVal = 0;
        int defendNum = 0;
        for (int i=0;i<nums.length;i++) {
            // 如果防守者数量为空，就取当前位置的值作为防守者
            if(0==defendNum) {
                defendVal = nums[i];
                defendNum = 1;
            } else {
                defendNum += defendVal==nums[i] ? 1 : -1;
            }
        }

        return defendVal;
    }


    public static void main(String[] args) {
        System.out.println(new L0169().majorityElement(new int[] {3,2,3}));
    }
}
