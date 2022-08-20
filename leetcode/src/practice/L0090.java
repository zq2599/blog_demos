package practice;

import java.util.*;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0090 {

    List<List<Integer>> res = new ArrayList<>();

    Set<String> fileter = new HashSet<>();

    private void dfs(int[] array, int startIndex, int[] path, int depth) {

        List<Integer> list = new ArrayList<>();


        StringBuilder sbud = new StringBuilder();
        // 收集数据
        for(int i=0;i<depth;i++) {
            list.add(path[i]);
            sbud.append(path[i]).append(",");
        }

        if (fileter.contains(sbud.toString())) {
            return;
        }

        fileter.add(sbud.toString());

        res.add(list);

        // 终止条件
        if (depth>(array.length-1)) {
            return;
        }

        for (int i=startIndex;i<array.length;i++) {
            path[depth] = array[i];
            dfs(array, i+1, path, depth+1);
        }
    }

    public List<List<Integer>> subsetsWithDup(int[] nums) {

        // 先排序
        Arrays.sort(nums);
        // 递归
        dfs(nums, 0, new int[nums.length], 0);

        return res;
    }

    public static void main(String[] args) {
        System.out.println(new L0090().subsetsWithDup(new int[]{1,2,2}));
        System.out.println("***");

    }
}
