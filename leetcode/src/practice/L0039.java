package practice;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0039 {

    List<List<Integer>> res = new LinkedList<>();

    private void dfs(int[] candidates, int target, int startIndex, Deque<Integer> path, int total) {
        // 这个方案的核心就是startIndex，
        // 以[1,2]为例，在第一层，for循环会将1,2都执行一遍，
        // 在执行1的时候，会得到两个结果：[1,1]，[1,2]，
        // 所以，在执行2的时候，[2,1]就没必要了，也就是说，执行2的时候，执行的dfs内部的for循环，应该从2开始！
        for (int i=startIndex;i<candidates.length;i++) {
            total += candidates[i];

            // 终止条件，搜集结果
            if(target==total) {
                path.addLast(candidates[i]);
                res.add(new LinkedList<>(path));
                total -= candidates[i];
                path.removeLast();
                continue;
            } else if(target<total){
                total -= candidates[i];
                continue;
            }

            path.addLast(candidates[i]);

            dfs(candidates, target, i, path, total);
            total -= candidates[i];
            path.removeLast();
        }
    }

    // 不是全排列！！！不要在dfs中每次for循环都从头开始！
    // 不要想着用set做排重，因为同一个数字，在不同的方案中出现的次数不一样，而set分不清！
    public List<List<Integer>> combinationSum(int[] candidates, int target) {
        dfs(candidates, target, 0, new ArrayDeque<>(), 0);
        return res;
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
//        List<List<Integer>> res = new L0039().combinationSum(new int[]{27,25,45,32,29,26,44,23,39,37,31,28,20,34,33,42,35,41,47,38,43,22,48,36,40,46,21,30,49},74);
        List<List<Integer>> res = new L0039().combinationSum(new int[]{2,3,5},8);
        for(List<Integer> list : res) {
            Tools.printOneLine(list);
        }
        System.out.println("use time [" + (System.currentTimeMillis()-start) + "]ms");
    }
}
