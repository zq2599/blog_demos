package practice;

/**
 * @program: leetcode
 * @description: 快排
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0101 {

    public boolean isSymmetric(TreeNode root) {
        return isMirror(root.left, root.right);
    }

    private boolean isMirror(TreeNode left, TreeNode right) {
        if (null==left && null==right) {
            return true;
        }

        // 如果只有一个为空，那就不是镜像
        if (null==left || null==right) {
            return false;
        }

        if (left.val!= right.val) {
            return false;
        }

        return isMirror(left.left, right.right)
            && isMirror(left.right, right.left);
    }

    public static void main(String[] args) {
        TreeNode t1 = new TreeNode(1);

        TreeNode t2 = new TreeNode(2);
        TreeNode t3 = new TreeNode(2);

        TreeNode t4 = new TreeNode(3);
        TreeNode t5 = new TreeNode(4);
        TreeNode t6 = new TreeNode(4);
        TreeNode t7 = new TreeNode(3);

        t1.left = t2;
        t1.right = t3;

        t2.left = t4;
        t2.right = t5;

        t3.left = t6;
        t3.right = t7;

        System.out.println(new L0101().isSymmetric(t1));
    }
}
