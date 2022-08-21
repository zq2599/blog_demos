package interview;

public class L1617 {

    public int maxSubArray(int[] nums) {

        if (1==nums.length) {
            return nums[0];
        }

        // dp[i]表示，选中i之后最大和，
        // 如果前提是必须使用nums[i]，那么dp[i]只有两种情况：dp[i-1]和nums[i]
        int last = nums[0];
        int current;

        // 注意，应该现在nums[0]作为初始max
        int max = nums[0];

        for (int i=1;i<nums.length;i++) {
            current = Math.max(last + nums[i], nums[i]);
            if(max<current) {
                max = current;
            }

            last = current;
        }

        return max;
    }

    public static void main(String[] args) {
//        System.out.println(new L1617().maxSubArray(new int[]{-2,1,-3,4,-1,2,1,-5,4}));
        System.out.println(new L1617().maxSubArray(new int[]{-1, -2}));
    }
}
