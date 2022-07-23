package practice;

import java.util.Arrays;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0283 {

    public void moveZeroes(int[] nums) {
        // 处理异常情况
        if(nums.length<1) {
            return;
        }

        // insertOffset表示从后往前移动非零数据时，可以写入的位置
        int insertOffset = -1;

        // 第一个可以写入的位置，是0第一次出现的位置
        for (int i=0;i<nums.length;i++) {
            if(nums[i]==0) {
                insertOffset = i;
                break;
            }
        }

        // 如果insertOffset还等于-1，就表示整个数组都没有0
        // 如果此时insertOffset指向队伍末尾，就表示数组最后一个元素是0，前面都不是0，此时就没必要操作了
        if(insertOffset==-1 || insertOffset==nums.length-1) {
            return;
        }

        for (int i=insertOffset+1;i<nums.length;i++) {
            // 遇到0，继续往后走，insertOffset不变，就是最靠左的可以写入的位置
            if (nums[i]==0) {
                continue;
            }

            nums[insertOffset] = nums[i];

            // 移动完毕后，此位置设为为0，这是题目要求
            nums[i] = 0;

            // 假设insertOffset是2，那么上一步将非零数字写入到2位置之后，接下来能写入的位置肯定是3，
            // 因为此刻的3位置的值，如果是0就能写入，如果非零上一步已经将其值写入到2位置的了，此3位置的内容就没用了，可以用于下一次写入
            insertOffset++;
        }
    }




    public static void main( String[] args ) {
        int[] nums = {0,1,0,3,12};


        new L0283().moveZeroes(nums);

        System.out.println(Arrays.toString(nums) );
    }
}
