package practice;

/**
 * @program: leetcode
 * @description: 快排
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0053 {

    /*
    public int maxSubArray(int[] nums) {
        // 异常处理
        if (nums.length<1) {
            return 0;
        }


        // 以array=[-2,4,-3,-1]这样的数组为例，
        // 从array[2]的视角去看，它只会关注连上array[1]之后，对array[1]这个位置的总和能不能带给它总和的增加，
        // array[2]却不关注array[1]和array[0]到底连接了没有，
        // 所以，假设dp[i]是i位置的最大总和，那么array[i+1]只关注dp[i]加上自己会不会更大，若会，就应该连上array[i]，也就是dp[i+i]=dp[i]+array[i+1]

        // 以array=[-2,4,-3,-1]为例，
        // dp[0]=-2，
        // dp[1]=4(不连前面)，
        // dp[2]=1(连前面)，可见，array[1]和array[2]相连后，总和大于array[2]
        // dp[3]=0(连前面)，可见，尽管array[2]小鱼0，但是

        int[] dp = new int[nums.length];

        dp[0] = nums[0];

        int max = dp[0];

        for(int i=1;i<nums.length;i++) {
            dp[i] = dp[i-1]<0 ? nums[i] : nums[i] + dp[i-1];

            if(max<dp[i]) {
                max = dp[i];
            }
        }

        return max;
    }
    */

    public int maxSubArray(int[] nums) {
        // 异常处理
        if (nums.length<1) {
            return 0;
        }

        // 以array=[-2,4,-3,-1]这样的数组为例，
        // 从array[2]的视角去看，它只会关注连上array[1]之后，对array[1]这个位置的总和能不能带给它总和的增加，
        // array[2]却不关注array[1]和array[0]到底连接了没有，
        // 所以，假设dp[i]是i位置的最大总和，那么array[i+1]只关注dp[i]加上自己会不会更大，若会，就应该连上array[i]，也就是dp[i+i]=dp[i]+array[i+1]

        // 以array=[-2,4,-3,-1]为例，
        // dp[0]=-2，
        // dp[1]=4(不连前面)，
        // dp[2]=1(连前面)，可见，array[1]和array[2]相连后，总和大于array[2]
        // dp[3]=0(连前面)，可见，尽管array[2]小鱼0，但是


        int prevDp = nums[0];
        int max = prevDp;

        for(int i=1;i<nums.length;i++) {
            // 其实括号左边是current的意思，不过这样写就把current变量省去了
            prevDp = prevDp<0 ? nums[i] : nums[i] + prevDp;
            // 找到更大的就更新了
            if(max<prevDp) {
                max = prevDp;
            }
        }

        return max;
    }

    public static void main(String[] args) {
        System.out.println(new L0053().maxSubArray(new int[] {5,4,-1,7,8}));
    }
}
