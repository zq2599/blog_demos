package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0112 {

    private boolean dfs(TreeNode root, int addValue, int target) {
        if (null==root) {
            return false;
        }

        // 注意两点:
        // 1. 审题，必须是到叶子结点
        // 2. 正负数都有，所以不适合用大于小于来提前结束
        if (null==root.left && null==root.right) {
            return target==(addValue+root.val);
        }

        if (dfs(root.left, addValue+root.val, target)) {
            return true;
        }

        if (dfs(root.right, addValue+root.val, target)) {
            return true;
        }

        return false;
    }

    public boolean hasPathSum(TreeNode root, int targetSum) {
        return dfs(root, 0, targetSum);
    }



    public static void main(String[] args) {
        TreeNode t1 = new TreeNode(5);
        TreeNode t2 = new TreeNode(4);
        TreeNode t3 = new TreeNode(8);
        TreeNode t4 = new TreeNode(11);
        TreeNode t5 = new TreeNode(13);
        TreeNode t6 = new TreeNode(4);
        TreeNode t7 = new TreeNode(7);
        TreeNode t8 = new TreeNode(2);
        TreeNode t9 = new TreeNode(1);

        t1.left = t2;
        t1.right = t3;

        t2.left = t4;

        t3.left = t5;
        t3.right = t6;

        t4.left= t7;
        t4.right = t8;

        t6.right = t9;

        t3.left = t6;
        t3.right = t7;

        System.out.println(new L0112().hasPathSum(t1, 22)); // true

        t1 = new TreeNode(1);
        t2 = new TreeNode(2);
        t3 = new TreeNode(3);
        t1.left = t2;
        t1.right = t3;

        System.out.println(new L0112().hasPathSum(t1, 5)); // fasle

        System.out.println(new L0112().hasPathSum(null, 0)); // fasle

        t1 = new TreeNode(1);
        t2 = new TreeNode(2);
        t1.left = t2;
        System.out.println(new L0112().hasPathSum(t1, 1));
    }
}
