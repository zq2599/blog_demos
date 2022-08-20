package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0113 {


    List<List<Integer>> res = new ArrayList<>();

    private void dfs(TreeNode root, int targetSum, ArrayList<Integer> path) {
        // 终止条件
        if (null==root) {
            return;
        }

        path.add(root.val);

        // 注意审题：结束的节点必须是叶子节点！！！
        // 所以终止条件只能是到达叶子节点，
        // 如果没到叶子节点，哪怕值等于targetSum了，也要继续dfs，因为下面的路径上的值之和如果等于零，那么总和还是targetSum，所以不能提前结束
        if (null==root.left && null==root.right) {
            int val = 0;
            for (int i=0;i<path.size();i++) {
                val += path.get(i);
            }

            if (val==targetSum) {
                res.add(new ArrayList<>(path));
            }
        } else {
            dfs(root.left, targetSum, path);
            dfs(root.right, targetSum, path);
        }

        path.remove(path.size()-1);
    }


    public List<List<Integer>> pathSum(TreeNode root, int targetSum) {
        dfs(root, targetSum, new ArrayList<>());
        return res;
    }

    public static void main(String[] args) {

        TreeNode t0 = new TreeNode(5);
        TreeNode t1 = new TreeNode(4);
        TreeNode t2 = new TreeNode(8);
        TreeNode t3 = new TreeNode(11);
        TreeNode t5 = new TreeNode(13);
        TreeNode t6 = new TreeNode(4);
        TreeNode t7 = new TreeNode(7);
        TreeNode t8 = new TreeNode(2);
        TreeNode t11 = new TreeNode(5);
        TreeNode t12 = new TreeNode(1);

        t0.left = t1;
        t0.right = t2;

        t1.left = t3;
        t2.left = t5;
        t2.right = t6;

        t3.left = t7;
        t3.right = t8;
        t6.left = t11;
        t6.right = t12;

        System.out.println(new L0113().pathSum(t0, 22));

        t0 = new TreeNode(1);
        t1 = new TreeNode(2);
        t2 = new TreeNode(3);

        t0.left = t1;
        t0.right = t2;
        System.out.println(new L0113().pathSum(t0, 5));
        System.out.println(new L0113().pathSum(t0, 0));


/*
        TreeNode t0 = new TreeNode(1);
        TreeNode t1 = new TreeNode(2);
        t0.left = t1;
        System.out.println(new L0113().pathSum(t0, 1));
*/
        /*
        TreeNode t0 = new TreeNode(-2);
        TreeNode t2 = new TreeNode(-3);
        t0.right = t2;
        System.out.println(new L0113().pathSum(t0, -5));
        */

        /*
        TreeNode t0 = new TreeNode(1);
        TreeNode t1 = new TreeNode(-2);
        TreeNode t2 = new TreeNode(-3);
        TreeNode t3 = new TreeNode(1);
        TreeNode t4= new TreeNode(3);
        TreeNode t5 = new TreeNode(-2);
        TreeNode t6 = null;
        TreeNode t7 = new TreeNode(-1);

        t0.left= t1;
        t0.right = t2;
        t1.left = t3;
        t1.right = t4;
        t2.left = t5;
        t2.right = t6;
        t3.left = t7;
        System.out.println(new L0113().pathSum(t0, -1));
        */

    }
}
