package interview;

public class L1716 {

    public int massage(int[] nums) {
        if (null==nums || 0==nums.length) {
            return 0;
        }

        if(1==nums.length) {
            return nums[0];
        }

        if (2==nums.length) {
            return Math.max(nums[0], nums[1]);
        }

        int[] maxVals = new int[nums.length];
        // maxVals的定义是：dp数组中，前i个的最大值
        maxVals[0] = nums[0];

        // 注意，这里要选最大值!
        maxVals[1] = Math.max(nums[0], nums[1]);

        int max = maxVals[1];

        int current;

        for (int i=2;i<nums.length;i++) {
            current = maxVals[i-2] + nums[i];

            if (max<current) {
                max = current;
            }

            maxVals[i] = max;
        }

        return max;
    }

    public static void main(String[] args) {
//        System.out.println(new L1617().maxSubArray(new int[]{-2,1,-3,4,-1,2,1,-5,4}));
//        System.out.println(new L1716().massage(new int[]{1,2,3,1}));
//        System.out.println(new L1716().massage(new int[]{2,7,9,3,1}));
//        System.out.println(new L1716().massage(new int[]{1,3,1}));
        System.out.println(new L1716().massage(new int[]{1,3,1,3,100}));
    }
}
