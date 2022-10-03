package practice;

import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: zq2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L1367 {

    public boolean isSubPath(ListNode head, TreeNode root) {
        return dfs(head, root);
    }

    private boolean dfs(ListNode head, TreeNode root) {
        if (null == root) {
            return false;
        }

        // 只要当前节点对上了，就继续对下一个节点
        if (root.val == head.val) {
            // 终止条件
            if (null == head.next) {
                return true;
            }

            if (dfs(head.next, root.left)) {
                return true;
            }

            if (dfs(head.next, root.right)) {
                return true;
            }
        }

        // 一旦对上，就立即返回true了
        if (dfs(head, root.left)) {
            return true;
        }

        return dfs(head, root.right);
    }

    public static void main(String[] args) {
        ListNode h1 = new ListNode(4);
        ListNode h2 = new ListNode(2);
        ListNode h3 = new ListNode(8);

        h1.next = h2;
        h2.next = h3;

        TreeNode a1 = new TreeNode(1);
        TreeNode a2 = new TreeNode(4);
        TreeNode a3 = new TreeNode(4);
        TreeNode a4 = new TreeNode(2);
        TreeNode a5 = new TreeNode(2);
        TreeNode a6 = new TreeNode(1);
        TreeNode a7 = new TreeNode(6);
        TreeNode a8 = new TreeNode(8);
        TreeNode a9 = new TreeNode(1);
        TreeNode a10 = new TreeNode(3);

        a1.left = a2;
        a1.right = a3;

        a2.right = a4;

        a3.left = a5;

        a4.left = a6;

        a5.left = a7;
        a5.right = a8;

        a8.left = a9;
        a8.right = a10;

        System.out.println(new L1367().isSubPath(h1, a1)); // true

        a1 = new TreeNode(1);
        a2 = new TreeNode(4);
        a3 = new TreeNode(4);
        a4 = new TreeNode(2);
        a5 = new TreeNode(2);
        a6 = new TreeNode(1);
        a7 = new TreeNode(6);
        a8 = new TreeNode(8);
        a9 = new TreeNode(1);
        a10 = new TreeNode(3);


        a1.left = a2;
        a1.right = a3;

        a2.right = a4;

        a3.left = a5;

        a4.left = a6;

        a5.left = a7;
        a5.right = a8;

        a8.left = a9;
        a8.right = a10;


         h1 = new ListNode(2);
         h2 = new ListNode(4);
         h3 = new ListNode(2);
         ListNode h4 = new ListNode(6);

         h1.next = h2;
         h2.next = h3;
         h3.next = h4;
         System.out.println(new L1367().isSubPath(h1, a1)); // true
    }
}
