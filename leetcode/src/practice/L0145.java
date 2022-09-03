package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0145 {

    List<Integer> res = new ArrayList<>();

    public List<Integer> postorderTraversal(TreeNode root) {
        if (null==root) {
            return new ArrayList<>();
        }



        postorderTraversal(root.left);
        postorderTraversal(root.right);
        res.add(root.val);

        return res;
    }



    public static void main(String[] args) {
        TreeNode t1 = new TreeNode(1);
        TreeNode t2 = null;
        TreeNode t3 = new TreeNode(2);
        TreeNode t4 = new TreeNode(3);

        t1.left = t2;
        t1.right = t3;
        t3.left = t4;
        System.out.println(new L0145().postorderTraversal(t1)); // 3,2,1

        t1 = new TreeNode(1);
        System.out.println(new L0145().postorderTraversal(t1)); // 1


    }
}
