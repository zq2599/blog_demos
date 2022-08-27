package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0045 {

    private int findFartestOneStep(int[] nums, int start) {

        int range = nums[start];

        // 如果是0，表示从start位置无法移动，返回的距离只能是自己的位置
        if (0==range) {
            return start;
        }

        int maxDistance = start+range;
        int bestPosition = start;

        for(int i=start+1;i<=start+range;i++) {
            if (nums[i]!=0 && i+nums[i]>maxDistance) {
                bestPosition = i;
                maxDistance = i+nums[i];
            }
            // 如果最远距离可以到达队尾，就不用再往后找了，直接范围当前位置
            if (maxDistance>=(nums.length-1)) {
                return i;
            }
        }

        return bestPosition;
    }

    public int jump(int[] nums) {
        if (1==nums.length) {
            return 0;
        } else if (2==nums.length) {
            // 注意审题：假设你总是可以到达数组的最后一个位置，所以数组等于2的时候，一定是一步结束
            return 1;
        }

        // if (nums[0]>=nums.length-1) {
        //     return 1;
        // }

        int currentPostion = 0;
        int step = 0;
        int nextPosition = 0;

        while (true) {
            // 从当前位置起步，如果一步就到达队尾，那么可以直接返回了（注意要把这一步加上）
            if ((currentPostion+nums[currentPostion])>= (nums.length-1)) {
                return step+1;
            }

            nextPosition = findFartestOneStep(nums, currentPostion);
//            System.out.println("从当前位置[" + currentPostion + "]探查，发现去[" + nextPosition + "]可以到达最远距离[" + (nextPosition+nums[nextPosition]) + "]");
            //走了一步
            currentPostion = nextPosition;
            step++;
        }
    }

    public static void main(String[] args) {
        // 2
        System.out.println(new L0045().jump(new int[] {2,3,1,1,4}));
//        // 2
        System.out.println(new L0045().jump(new int[] {2,3,0,1,4}));
//        // 1
        System.out.println(new L0045().jump(new int[] {3,2,1}));
//        // 2
        System.out.println(new L0045().jump(new int[] {7,0,9,6,9,6,1,7,9,0,1,2,9,0,3}));
        // 4
        System.out.println(new L0045().jump(new int[] {1,1,1,1,1}));

    }
}
