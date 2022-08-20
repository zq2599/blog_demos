package practice;

import com.sun.source.tree.Tree;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0095 {

    public List<TreeNode> generateTrees(int start, int end) {
        List<TreeNode> all = new ArrayList<>();
        if (start>end) {
            all.add(null);
            return all;
        }

        for (int i=start;i<=end;i++) {

            List<TreeNode> left = generateTrees(start, i-1);
            List<TreeNode> right = generateTrees(i+1,end);

            for(TreeNode leftTreeNode : left) {
                for(TreeNode rightTreeNode : right) {
                    TreeNode root = new TreeNode(i);
                    root.left = leftTreeNode;
                    root.right = rightTreeNode;

                    all.add(root);
                }
            }
        }

        return all;
    }



    public List<TreeNode> generateTrees(int n) {
        return generateTrees(1, n);
    }

    public static void main(String[] args) {
        List<TreeNode> list = new L0095().generateTrees(3);

        for (TreeNode treeNode : list) {
            Tools.print(treeNode);
            System.out.println("");
        }
    }
}
