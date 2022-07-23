package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0543 {

    private int max = 0;

    public int diameterOfBinaryTree(TreeNode root) {
        if(null==root.left && null==root.right) {
            return 0;
        }

        // 这道题的核心，是理解直径的本质：某个节点左子树深度与右子树深度之和
        depth(root);
        return max;
    }

    private int depth(TreeNode root) {
        if (null==root) {
            return 0;
        }

        int left = depth(root.left);
        int right = depth(root.right);

        if ((left+right)>max) {
            max = left + right;
        }

        return Math.max(left, right) + 1;
    }

    public static void main(String[] args) {
        TreeNode t1 = new TreeNode(1);
        TreeNode t2 = new TreeNode(2);
        TreeNode t3 = new TreeNode(3);
        TreeNode t4 = new TreeNode(4);
        TreeNode t5 = new TreeNode(5);

        t1.left = t2;
        t1.right = t3;

        t2.left = t4;
        t2.right = t5;

        System.out.println(new L0543().diameterOfBinaryTree(t1));
    }
}
