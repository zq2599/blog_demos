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
public class L0047 {

    List<List<Integer>> res = new ArrayList<>();



    public void dfs(int[] nums, boolean[] used, int[] path, int depth) {
        // 终止条件(深度达到)
        // 搜集：栈入res
        // 本题的终止条件是一次组合的长度达到数组长度
        if (depth==nums.length) {
            // 搜集结果
            // 千万注意：这个path一直在使用中，所以不能把path放入res中，而是path的元素
//            res.add(Arrays.stream(path).boxed().collect(Collectors.toList()));


            List<Integer> list = new ArrayList<>();
            for(int val : path) {
                list.add(val);
            }

            res.add(list);
            return;
        }

        boolean doDfsFlag[] = new boolean[nums.length];

        for (int i=0;i<nums.length;i++) {
            // 如果当前值和前一个值一模一样，并且通过doDfsFlag[i-1]可以确认前一个值执行过dfs，那么当前值就没必要执行了，因为前面已经做过了
            if (i>0 && nums[i]==nums[i-1] && doDfsFlag[i-1]) {
                // 千万注意：这里要给doDfsFlag[i]设置为true，
                // 假设有[1,1,1]，在处理第一个之后，doDfsFlag[0]=true
                // 在处理第二个的时候，发现doDfsFlag[0]为true，于是第二个就不用执行了，
                // 在处理第三个的时候，检查的是doDfsFlag[1]！！！
                // 所以，处理第二个的时候，要设置doDfsFlag[1]=true，不然第三个发现doDfsFlag[1]=false，就不会走这里的continue了
                doDfsFlag[i] = true;
                continue;
            }

            // 如果当前数字没有用到，就标记，进入path，再做dfs
            if (!used[i]) {
                used[i] = true;
                // 注意，path的下标千万不要用i！
                // 例如1和2的全排列，在制造[2,1]的时候，i=1，但此时要修改的是path[i]，
                // 所以path的下标应该是depth
                path[depth] = nums[i];
                // 一次递归，深度要加一
                dfs(nums, used, path, depth+1);
                used[i] = false;

                // 表示在for循环中，第i位执行过完整的dfs
                doDfsFlag[i] = true;
            }
        }
    }

    public List<List<Integer>> permute(int[] nums) {
        Arrays.sort(nums);
        // 回溯过程中的重要辅助参数：标记nums数组中有哪些已经使用过
        boolean[] used = new boolean[nums.length];
        // 回溯过程中的重要辅助参数：路径
        int[] path = new int[nums.length];

        dfs(nums, used, path, 0);
        return res;
    }

    public static void main(String[] args) {
        List<List<Integer>> list = new L0047().permute(new int[] {3,3,0,3});

        list.forEach(one -> {
            Tools.printOneLine(one);
        });

    }
}
