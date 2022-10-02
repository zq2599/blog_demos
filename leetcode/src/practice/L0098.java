package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0098 {


    public boolean isValidBST(TreeNode root) {

        // 左子树，只要求不能比自己大，至于小到什么程度都无所谓，所以用int最小值作为左侧边界
        if (!isValidBST(root.left, Long.MIN_VALUE, root.val)) {
            return false;
        }

        // 右子树，只要求不能比自己小，至于达到什么程度都无所谓，所以用int最大值作为右侧边界
        if (!isValidBST(root.right, root.val, Long.MAX_VALUE)) {
            return false;
        }

        return true;
    }

    public boolean isValidBST(TreeNode root, long min, long max) {
        if (null==root) {
            return true;
        }

        // 检查自己
        // 注意审题：左侧必须比自己小，不接受等于，右侧必须必自己大，不接受等于
        if (root.val<=min || root.val>=max) {
            return false;
        }

        // 左子树，只要求不能比自己大，至于小到什么程度都无所谓，所以用入参的最小值作为左侧边界
        if (!isValidBST(root.left, min, Math.min(max, root.val))) {
            return false;
        }

        // 右子树，只要求不能比自己小，至于达到什么程度都无所谓，所以用入参的最大值作为右侧边界
        if (!isValidBST(root.right, Math.max(min, root.val), max)) {
            return false;
        }

        return true;
    }





    public static void main(String[] args) {
        TreeNode t1 = new TreeNode(2);
        TreeNode t2 = new TreeNode(1);
        TreeNode t3 = new TreeNode(3);

        t1.left = t2;
        t1.right = t3;

        System.out.println(new L0098().isValidBST(t1)); // true

        t1 = new TreeNode(5);
        t2 = new TreeNode(1);
        t3 = new TreeNode(4);
        TreeNode t4 = new TreeNode(3);
        TreeNode t5 = new TreeNode(6);

        t1.left = t2;
        t1.right = t3;
        t3.left = t4;
        t3.right = t5;

        System.out.println(new L0098().isValidBST(t1)); // false

        t1 = new TreeNode(4);
        t2 = new TreeNode(5);
        t3 = new TreeNode(6);
        t4 = new TreeNode(3);
        t5 = new TreeNode(7);

        t1.left = t2;
        t1.right = t3;
        t3.left = t4;
        t3.right = t5;

        System.out.println(new L0098().isValidBST(t1)); // false

        t1 = new TreeNode(2);
        t2 = new TreeNode(2);
        t3 = new TreeNode(2);
        t1.left = t2;
        t1.right = t3;

        System.out.println(new L0098().isValidBST(t1)); // false

        t1 = new TreeNode(-2147483648);
        t2 = null;
        t3 = new TreeNode(2147483647);
        t1.left = t2;
        t1.right = t3;

        System.out.println(new L0098().isValidBST(t1)); // true
    }
}
