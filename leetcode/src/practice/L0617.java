package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0617 {

    public TreeNode mergeTrees(TreeNode root1, TreeNode root2) {
        if (null==root1) {
            return root2;
        }

        if (null==root2) {
            return root1;
        }

        merge(null, root1, null, root2);
        return root1;
    }

    private void merge(TreeNode parent1, TreeNode node1, TreeNode parent2, TreeNode node2) {
        // 终止条件
        if (null==node1 && null==node2) {
            return;
        }

        // 如果node1为空，就要造一个节点，给parent1接上，
        // 至于是左子节点还是右子节点，可以根据node2和parent2的关系来判断，因为两个树是同步处理的
        if (null==node1) {
            // 要保证node1节点的正常
            node1 = new TreeNode(node2.val);

            // 至于node1应该是
            if (parent2.left==node2) {
                parent1.left = node1;
            } else {
                parent1.right = node1;
            }
        } else if (null==node2) {
            // 因为策略是将node2的内容合入node1，所以node2等于null的时候无需处理
        } else {
           node1.val += node2.val;
        }

        // 注意，如果node2等于空，就不能写node2.left或者node2.right
        if (null==node2) {
            merge(node1, node1.left, node2, null);
            merge(node1, node1.right, node2, null);
        } else {
            merge(node1, node1.left, node2, node2.left);
            merge(node1, node1.right, node2, node2.right);
        }

    }

    public static void main(String[] args) {
        TreeNode t11 = new TreeNode(1);
        TreeNode t12 = new TreeNode(3);
        TreeNode t13 = new TreeNode(2);
        TreeNode t14 = new TreeNode(5);

        t11.left = t12;
        t11.right = t13;

        t12.left = t14;

        TreeNode t21 = new TreeNode(2);
        TreeNode t22 = new TreeNode(1);
        TreeNode t23 = new TreeNode(3);
        TreeNode t25 = new TreeNode(4);
        TreeNode t27 = new TreeNode(7);

        t21.left = t22;
        t21.right = t23;

        t22.right = t25;
        t23.right = t27;

        System.out.println(new L0617().mergeTrees(t11, t21));
    }
}
