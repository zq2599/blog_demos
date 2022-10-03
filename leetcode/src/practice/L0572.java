package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: zq2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0572 {

    
    public boolean isSubtree(TreeNode root, TreeNode subRoot) {
        // 假设root只有一个节点12，subRoot只有一个节点2，用本题的解法返回的是true
        // 所以，字符串起始就要有个符号
        StringBuilder sRoot = new StringBuilder(",");
        build(root, sRoot);

        StringBuilder sSubRoot = new StringBuilder(",");
        build(subRoot, sSubRoot);

        // System.out.println(sRoot);
        // System.out.println(sSubRoot);

        return sRoot.toString().indexOf(sSubRoot.toString())>-1;
    }

    private void build(TreeNode root, StringBuilder sbud) {
        if (null==root) {
            sbud.append("-,");
            return;
        }

        sbud.append(root.val).append(",");
        build(root.left, sbud);
        build(root.right, sbud);
    }



    public static void main(String[] args) {
        TreeNode a1 = new TreeNode(3);
        TreeNode a2 = new TreeNode(4);
        TreeNode a3 = new TreeNode(5);
        TreeNode a4 = new TreeNode(1);
        TreeNode a5 = new TreeNode(2);
        a1.left = a2;
        a1.right = a3;
        a2.left = a4;
        a2.right = a5;

        TreeNode b1 = new TreeNode(4);
        TreeNode b2 = new TreeNode(1);
        TreeNode b3 = new TreeNode(2);
        b1.left = b2;
        b1.right = b3;

        System.out.println(new L0572().isSubtree(a1, b1)); // true

        a1 = new TreeNode(12);
        b1 = new TreeNode(2);
        System.out.println(new L0572().isSubtree(a1, b1)); // false
        System.out.println("");
    }
}
