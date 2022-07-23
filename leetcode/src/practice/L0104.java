package practice;

/**
 * @program: leetcode
 * @description: 快排
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0104 {

    public int maxDepth(TreeNode root) {
        return depth(root);
    }

    private int depth(TreeNode root) {
        if (null==root) {
            return 0;
        }

        return Math.max(depth(root.left), depth(root.right)) + 1;
    }


    public static void main(String[] args) {
        TreeNode t1 = new TreeNode(3);

        TreeNode t2 = new TreeNode(9);
        TreeNode t3 = new TreeNode(20);

        TreeNode t6 = new TreeNode(15);
        TreeNode t7 = new TreeNode(7);

        t1.left = t2;
        t1.right = t3;


        t3.left = t6;
        t3.right = t7;

        System.out.println(new L0104().maxDepth(t1));
    }
}
