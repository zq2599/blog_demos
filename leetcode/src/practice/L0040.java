package practice;

import java.util.*;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0040 {

    List<List<Integer>> res = new LinkedList<>();

    private void dfs(int[] candidates, int target, int startIndex, Deque<Integer> path, int total) {
        // 这个方案的核心就是startIndex，
        // 以[1,2]为例，在第一层，for循环会将1,2都执行一遍，
        // 在执行1的时候，会得到两个结果：[1,1]，[1,2]，
        // 所以，在执行2的时候，[2,1]就没必要了，也就是说，执行2的时候，执行的dfs内部的for循环，应该从2开始！
        for (int i=startIndex;i<candidates.length;i++) {
            // 配合图一起看，可知，在同一个循环中，如果当前值和前面的值相等，那么接下来要做的事情，前面其实已经做了！
            if (i>startIndex && candidates[i]==candidates[i-1]) {
                continue;
            }

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
                break;
            }

            path.addLast(candidates[i]);

            dfs(candidates, target, i+1, path, total);

            total -= candidates[i];
            path.removeLast();
        }
    }

    // 不是全排列！！！不要在dfs中每次for循环都从头开始！
    // 不要想着用set做排重，因为同一个数字，在不同的方案中出现的次数不一样，而set分不清！
    public List<List<Integer>> combinationSum(int[] candidates, int target) {
        Arrays.sort(candidates);
        dfs(candidates, target, 0, new ArrayDeque<>(), 0);
        return res;
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
//        List<List<Integer>> res = new L0040().combinationSum(new int[]{10,1,2,7,6,1,5},8);
        List<List<Integer>> res = new L0040().combinationSum(new int[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},30);
        for(List<Integer> list : res) {
            Tools.printOneLine(list);
        }
        System.out.println("use time [" + (System.currentTimeMillis()-start) + "]ms");
    }
}
