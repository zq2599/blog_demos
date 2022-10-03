package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: zq2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0100 {

    
    public boolean isSameTree(TreeNode p, TreeNode q) {
        List<Integer> forCheck = new ArrayList<>();

        // 构建第一个
        build(p, forCheck);

        return buildAndCheck(q, new ArrayList<>(), forCheck);
    }


    private void build(TreeNode treeNode, List<Integer> list) {

        if (null==treeNode) {
            list.add(null);
            return;
        }

        list.add(treeNode.val);

        build(treeNode.left, list);
        build(treeNode.right, list);
    }

    private boolean buildAndCheck(TreeNode treeNode, List<Integer> list, List<Integer> forCheck) {

        int size;

        if (null==treeNode) {
            
            size = list.size();

            if (forCheck.size()<=size || forCheck.get(size)!=null) {
                return false;
            }
            
            list.add(null);
            return true;
        }

        size = list.size();

        if (forCheck.size()<=size 
        || null==forCheck.get(size)
        || forCheck.get(size)!=treeNode.val) {
            return false;
        }

        list.add(treeNode.val);

        if (!buildAndCheck(treeNode.left, list, forCheck)){
            return false;
        }

        return buildAndCheck(treeNode.right, list, forCheck);
    }


    public static void main(String[] args) {
        TreeNode a1 = new TreeNode(1);
        TreeNode a2 = new TreeNode(2);
        TreeNode a3 = new TreeNode(3);
        a1.left = a2;
        a1.right = a3;

        TreeNode b1 = new TreeNode(1);
        TreeNode b2 = new TreeNode(2);
        TreeNode b3 = new TreeNode(3);
        b1.left = b2;
        b1.right = b3;

        System.out.println(new L0100().isSameTree(a1, b1)); // true


        System.out.println("");
    }
}
