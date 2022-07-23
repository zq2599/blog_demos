package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0094 {

    public List<Integer> inorderTraversal(TreeNode root) {

        List<Integer> list = new ArrayList<>();

        add(list, root);

        return list;
    }

    private void add(List<Integer> list, TreeNode root) {
        // 中序遍历是左根右

        // 递归的结束条件
        if (null==root) {
            return;
        }

        // 左
        add(list, root.left);

        // 根
        list.add(root.val);

        // 右
        add(list, root.right);
    }



    public static void main(String[] args) {
        TreeNode t1 = new TreeNode(1);
        TreeNode t2 = new TreeNode(2);
        TreeNode t3 = new TreeNode(3);
        t1.right = t2;
        t2.left = t3;

        Tools.print(new L0094().inorderTraversal(t1));
    }
}
