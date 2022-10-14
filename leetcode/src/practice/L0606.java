package practice;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0606 {

    
    /*
    public String tree2str(TreeNode root) {
        
        StringBuilder sbud = new StringBuilder();
        sbud.append(root.val);

        if (null!=root.left && null!=root.right) {
            sbud.append("(")
                .append(tree2str(root.left))
                .append(")")
                .append("(")
                .append(tree2str(root.right))
                .append(")");
        } else if (null!=root.left && null==root.right) {
            sbud.append("(")
            .append(tree2str(root.left))
            .append(")");
        } else if (null==root.left && null!=root.right) {
            sbud.append("()(")
            .append(tree2str(root.right))
            .append(")");
        } 

        return sbud.toString();
    }
    */

    public String tree2str(TreeNode root) {
        sbud = new StringBuilder();
        dfs(root);
        return sbud.toString();
    }

    private StringBuilder sbud;

    private void dfs(TreeNode root) {
        sbud.append(root.val);

        if (null!=root.left && null!=root.right) {
            sbud.append("(");

            dfs(root.left);

            sbud.append(")")
                .append("(");

            dfs(root.right);

            sbud.append(")");

        } else if (null!=root.left && null==root.right) {
            sbud.append("(");
            dfs(root.left);
            sbud.append(")");
        } else if (null==root.left && null!=root.right) {
            sbud.append("()(");
            dfs(root.right);
            sbud.append(")");
        }
    }

   
    public static void main(String[] args) {
        TreeNode t1 = new TreeNode(1);
        TreeNode t2 = new TreeNode(2);
        TreeNode t3 = new TreeNode(3);
        TreeNode t4 = new TreeNode(4);


        t1.left = t2;
        t1.right = t3;
        t2.left = t4;


        System.out.println(new L0606().tree2str(t1));
        System.out.println("");


        t1 = new TreeNode(1);
        t2 = new TreeNode(2);
        t3 = new TreeNode(3);
        t4 = new TreeNode(4);

        t1.left = t2;
        t1.right = t3;
        t2.right = t4;

        System.out.println(new L0606().tree2str(t1));
        System.out.println("");
    }
}
