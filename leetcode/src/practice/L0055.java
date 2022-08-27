package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0055 {

    public boolean canJump(int[] nums) {
        if (1==nums.length) {
            return true;
        } else if (0==nums[0]){
            return false;
        }

        int maxDistance = nums[0];
        int currentDistance;

        // 注意结束条件是i<(nums.length-1)，也就是说nums数组最后一个值
        for(int i=0;i<(nums.length-1);i++) {

            // 一步也走不出去，所以不参与最远距离计算
            if(0==nums[i]) {
                // 如果最大距离迈不过当前位置，只能返回false了
                if (maxDistance<=i) {
                    return false;
                }
                continue;
            }

            currentDistance = i + nums[i];

            // 能走到最后，直接返回true了
            if (currentDistance>=(nums.length-1)) {
                return true;
            }

            if (maxDistance<currentDistance) {
                maxDistance = currentDistance;
            }
        }

        return maxDistance > nums.length;
    }



    public static void main(String[] args) {
        // true
        System.out.println(new L0055().canJump(new int[] {2,3,1,1,4}));
        // false
        System.out.println(new L0055().canJump(new int[] {3,2,1,0,4}));
        // 解答错误，预期是false
        System.out.println(new L0055().canJump(new int[] {1,0,1,0}));
        // 预期是true
        System.out.println(new L0055().canJump(new int[] {2,0,0}));
        // 预期是true
        System.out.println(new L0055().canJump(new int[] {0}));
    }
}
