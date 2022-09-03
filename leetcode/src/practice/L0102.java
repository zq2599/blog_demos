package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0102 {

    List<List<Integer>> res = new ArrayList<>();

    public List<List<Integer>> levelOrder(TreeNode root) {
        if (null==root) {
            return res;
        }

        List<Integer> rootList = new ArrayList<>();
        rootList.add(root.val);
        res.add(rootList);

        if(null!=root.left || null!=root.right) {
            travel(root, 1);
        }

        return res;
    }


    private void travel(TreeNode root, int depth) {

        if (res.size()<(depth+1)) {
            res.add(new ArrayList<>());
        }

        List<Integer> current = res.get(depth);

        if (root.left!=null) {
            current.add(root.left.val);
            if (null!=root.left.left || null!=root.left.right) {
                travel(root.left, depth + 1);
            }
        }

        if (root.right!=null) {
            current.add(root.right.val);
            if (null!=root.right.left || null!=root.right.right) {
                travel(root.right, depth + 1);
            }
        }
    }



    public static void main(String[] args) {
        TreeNode t1 = new TreeNode(3);
        TreeNode t2 = new TreeNode(9);
        TreeNode t3 = new TreeNode(20);
        TreeNode t4 = null;
        TreeNode t5 = null;
        TreeNode t6 = new TreeNode(15);
        TreeNode t7 = new TreeNode(7);

        t1.left = t2;
        t1.right = t3;

        t3.left = t6;
        t3.right = t7;

        System.out.println(new L0102().levelOrder(t1)); // 3,2,1

        t1 = new TreeNode(1);
        System.out.println(new L0102().levelOrder(t1)); // 1


    }
}
