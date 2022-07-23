package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0448 {

    /*
    public List<Integer> findDisappearedNumbers(Integer[] nums) {
        List<Integer> rlt = new ArrayList<>();

        if (nums==null || nums.length<1) {
            return rlt;
        }

        // 本题的关键：数组元素的每一个值，减一之后，一定是个索引位置
        // 例如数组长度等于3，索引位置就是0，,1，,2
        // 该数组中的元素值，减一后，一定是0，1，2三者之一，
        // 所以，根据元素值，一定可以找到一个索引，例如元素值2，减一后就是1，一定有个索引等于1，
        // 基于以上规律，读取每个元素值，给对应的索引打标，读到1就给nums[0]打标，读到2就给nums[1]打标，读到3就给nums[3]打标,
        // 最后检查整个数组，没有打标的元素，一定是因为有个数字在数组中不存在，例如num[1]没打标，就表示数组中没有读到过2

        int max = nums.length;

        int index = 0;

        for (int i=0;i<max;i++) {
            // 题目中，元素值的大小不会超过数组长度，
            // 所以，如果元素大小超过数组长度，就意味着刚才已经打标了
            if(nums[i]>max) {
                // 需要去掉标签才是该元素的原始值
                index = nums[i]-max;
            } else {
                index = nums[i];
            }

            // 元素值一定对应着一个索引，
            // 现在看这个索引位置的值是否已经打标，如果没有，就打标
            if(nums[index-1]<=max) {
                // 所谓打标，就是加上max
                nums[index-1] += max;
            }
        }

        for (int i=0;i<max;i++) {
            if(nums[i]<=max) {
                rlt.add(i+1);
            }
        }

        return  rlt;
    }
     */

    public List<Integer> findDisappearedNumbers(Integer[] nums) {
        List<Integer> rlt = new ArrayList<>();

        if (nums==null || nums.length<1) {
            return rlt;
        }

        // 本题的关键：数组元素的每一个值，减一之后，一定是个索引位置
        // 例如数组长度等于3，索引位置就是0，,1，,2
        // 该数组中的元素值，减一后，一定是0，1，2三者之一，
        // 所以，根据元素值，一定可以找到一个索引，例如元素值2，减一后就是1，一定有个索引等于1，
        // 基于以上规律，读取每个元素值，给对应的索引打标，读到1就给nums[0]打标，读到2就给nums[1]打标，读到3就给nums[3]打标,
        // 最后检查整个数组，没有打标的元素，一定是因为有个数字在数组中不存在，例如num[1]没打标，就表示数组中没有读到过2

        int max = nums.length;
        int index;

        for (int i=0;i<max;i++) {
            // 千万注意，这里要先减去1，再取模
            // 如果先用nums[i]模max，再减一的话，当nums[i]等于max的时候，模完了就是0了，再减一的话结果等于-1，这是不符合要求的，
            // 不论之前加过几次max，取模后都能还原成加max之前的状态
            index = (nums[i] -1)%max;
            // index就是数组元素的原始值，该值一定是个索引位置
            nums[index] += max;
        }

        for (int i=0;i<max;i++) {
            if(nums[i]<=max) {
                rlt.add(i+1);
            }
        }

        return  rlt;
    }



    public static void main( String[] args ) {
        System.out.println(new L0448().findDisappearedNumbers(new Integer[]{4,3,2,7,8,2,3,1}));
    }
}
