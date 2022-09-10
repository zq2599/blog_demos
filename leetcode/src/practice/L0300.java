package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0300 {

    /*
    public int lengthOfLIS(int[] nums) {

        // dp[i]表示：以nums[i]作为结尾的最长子序列的长度，注意再注意，一定要以nums[i]结尾
        int[] dp = new int[nums.length];

        dp[0] = 1;
        int max = 1;

        for(int i=1;i<nums.length;i++) {
            // 所有比nums[i]小的数字，都可能用来连接nums[i]，
            // 以2,1,3为例，2连接3符合要求，1连接3也符合要求，
            // 这时候就要判断2连接3合适，还是1连接3合适
            for(int j=0;j<i;j++) {
                if(nums[j]<nums[i]) {
                    if(dp[j]>dp[i]) {
                        dp[i] = dp[j];
                    }
                }
            }

            // 加上自己这一位
            dp[i] += 1;

            // 所有dp中的最大值就是返回值
            if(max<dp[i]) {
                max = dp[i];
            }
        }

        return max;
    }
    */

    public int lengthOfLIS(int[] nums) {

        // 一个元素，代表一堆的最顶部
        int[] piles = new int[nums.length];
        piles[0] = nums[0];

        // 一共有几堆
        int pileNum = 1;

        // 左指针，右指针，中间指针
        int left, right, middle;

        // 处理每一个元素
        for (int i=1;i<nums.length;i++) {

            left = 0;
            right = pileNum;

            while(left<right) {
                middle = (left+right)/2;

                if (nums[i]<piles[middle]) {
                    right = middle;
                } else if (nums[i]>piles[middle]) {
                    left = middle+1;
                } else {
                    // 连续出现的数字，要移动右侧指针
                    // 不能移动左侧，因为左侧变大，会导致新增一个堆，答题结果会变大
                    right = middle;
                }
            }

            // 超出右边界的时候，表示数字过大，当前位置已经放不下了
            if (left==pileNum) {
                piles[pileNum++] = nums[i];
            } else {
                piles[left] = nums[i];
            }
        }

        return pileNum;
    }




    public static void main(String[] args) {
        System.out.println(new L0300().lengthOfLIS(new int[]{10,9,2,5,3,7,101,18})); // 4
        System.out.println(new L0300().lengthOfLIS(new int[]{0,1,0,3,2,3})); // 4
        System.out.println(new L0300().lengthOfLIS(new int[]{7,7,7,7,7,7,7})); // 1
        System.out.println(new L0300().lengthOfLIS(new int[]{4,10,4,3,8,9})); // 3
        System.out.println(new L0300().lengthOfLIS(new int[]{0,1,0,3,2,3})); // 4
        System.out.println(new L0300().lengthOfLIS(new int[]{6,3,5,10,11,9,14,13,7,4,8,12})); // 5
    }
}
