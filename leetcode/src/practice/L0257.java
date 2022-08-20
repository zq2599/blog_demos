package practice;

import java.util.ArrayList;
<<<<<<< HEAD
import java.util.LinkedList;
=======
>>>>>>> origin/master
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0257 {

<<<<<<< HEAD
    List<String> res = new ArrayList<>();

    private void dfs(TreeNode current, ArrayList<Integer> path) {

        // 终止条件，也就是收录条件
        if (null== current.left && null==current.right) {
            StringBuilder sbud = new StringBuilder();
            for (int i=0;i<path.size();i++) {
                sbud.append(path.get(i));
                sbud.append("->");
            }

            // 还要加上自己
            sbud.append(current.val);
            // 收录
            res.add(sbud.toString());

            return;
        }

        path.add(current.val);

        if (null!=current.left) {
            dfs(current.left, path);
        }

        if (null!=current.right) {
            dfs(current.right, path);
        }

        path.remove(path.size()-1);
    }

    public List<String> binaryTreePaths(TreeNode root) {
        dfs(root, new ArrayList<>());
        return res;
    }

    public static void main(String[] args) {
        TreeNode t0 = new TreeNode(1);
        TreeNode t1 = new TreeNode(2);
        TreeNode t2 = new TreeNode(3);
        TreeNode t3 = null;
        TreeNode t4 = new TreeNode(5);

        t0.left = t1;
        t0.right = t2;
        t1.right = t4;

        System.out.println((new L0257().binaryTreePaths(t0)));
=======

    List<List<Integer>> res = new ArrayList<>();

    private void dfs(int targetSum, int startIndex, int[] path, int depth) {

        int val = 0;

        for(int i=0;i<depth;i++) {
            val += path[i];
        }

        // 注意审题！！！
        // 必须要凑齐K个数
        if (depth==path.length) {

            if(targetSum==val) {
                List<Integer> list = new ArrayList<>();

                for(int i=0;i<depth;i++) {
                    list.add(path[i]);
                }

                res.add(list);
            }

            return;
        } else if (val>=targetSum){
            // 还没达到数量，累加和就已经等于目标值了，那就可以提前返回了
            return;
        }

        for (int i=startIndex;i<10;i++) {
            path[depth] = i;
            dfs(targetSum, i+1, path, depth+1);
        }
    }


    public List<String> binaryTreePaths(TreeNode root) {
        return null;
    }

    public static void main(String[] args) {



>>>>>>> origin/master
    }
}
