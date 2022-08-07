package practice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0078 {

    List<List<Integer>> rlt = new ArrayList<>();

    public List<List<Integer>> subsets(int[] nums) {

        boolean[] path = new boolean[21];

        // 答案中要包含空
        rlt.add(new ArrayList<>());

        if (1==nums.length) {
            rlt.add(new ArrayList<>(Arrays.asList(nums[0])));
            return rlt;
        }

        dfs(nums, 0, path,0);

        return rlt;
    }

    private void dfs(int[] nums, int startIndex, boolean[] usedFlag, int depth) {
        List<Integer> list = new ArrayList<>();
        for(int i=0;i<usedFlag.length;i++) {
            if (usedFlag[i]) {
                list.add(i-10);
            }
        }

        if (!list.isEmpty()) {
            rlt.add(list);
        }

        // 终止条件
        if (depth==nums.length || startIndex>=nums.length) {
            return;
        }

        for (int i=startIndex;i<nums.length;i++) {
            if (!usedFlag[nums[i]+10]) {
                usedFlag[nums[i]+10] = true;
                dfs(nums, i+1, usedFlag, depth+1);
                usedFlag[nums[i]+10] = false;
            }
        }
    }



    public static void main(String[] args) {
        System.out.println(new L0078().subsets(new int[] {1,2,3}));
    }
}
