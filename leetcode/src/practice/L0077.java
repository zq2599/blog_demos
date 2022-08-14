package practice;

import java.util.*;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0077 {

    List<List<Integer>> res = new ArrayList<>();

    private void dfs(int total, int startIndex, int[] path, int depth) {
        // 重要剪枝，当数组剩下的数字加上当前的path，凑不齐题目所需数字的时候，就没必要继续执行下去了
        if ((path.length - depth) > (total-startIndex + 1)) {
            return;
        }


        // 终止条件
        if (depth==path.length) {
            List<Integer> list = new ArrayList<>();
            for (int i : path) {
                list.add(i);
            }
            res.add(list);
            return;
        }

        for (int i=startIndex+1;i<=total;i++) {
            path[depth] = i;
            dfs(total, i, path, depth+1);
        }
    }

    public List<List<Integer>> combine(int n, int k) {
        dfs(n, 0, new int[k], 0);
        return res;
    }

    public static void main(String[] args) {
        System.out.println(new L0077().combine(4,2));
        System.out.println("***");
        System.out.println(new L0077().combine(1,1));

    }
}
