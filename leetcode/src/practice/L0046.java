package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0046 {

    List<List<Integer>> res = new ArrayList<>();

    public List<List<Integer>> permute(int[] nums) {
        // 回溯过程中的重要辅助参数：标记nums数组中有哪些已经使用过
        boolean[] used = new boolean[nums.length];
        // 回溯过程中的重要辅助参数：路径
        int[] path = new int[nums.length];

        dfs(nums, used, path, 0);
        return res;
    }

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

        // for循环
         // 当前i是否可用
           // 标记当前i已用
           // i入栈
           // dfs
           // i出栈
           // 标记当前i未用

        for (int i=0;i<nums.length;i++) {
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
            }
        }
    }

    public static void main(String[] args) {
        List<List<Integer>> list = new L0046().permute(new int[] {1,2,3});

        list.forEach(one -> {
           Tools.printOneLine(one);
        });

    }
}
