package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0106 {


    // 算法逻辑：从后续遍历的数组中，从后往前，挨个取出数字用于构建完整的树，
    // 所以，usedNum记录的是：已经从后续遍历数组的末尾取出了多少元素
    private int usedNum = 0;

    private static int find(int[] inorder, int from, int to, int val) {
        for(int i=from;i<=to;i++) {
            if (val==inorder[i]) {
                return i;
            }
        }

        return 0;
    }

    private void build(TreeNode root , boolean isLeft, int[] inorder, int from, int to, int[] postorder) {

        int subNodeVal = postorder[postorder.length-1-usedNum];
        // 这就是当前层的根节点
        TreeNode subNode = new TreeNode(subNodeVal);

        // 又用了一个
        usedNum++;

        // 1. 处理根
        if (isLeft) {
            root.left = subNode;
        } else  {
            root.right = subNode;
        }

        // 如果只有自己，就立即返回了
        if (from==to) {
            return;
        }

        // 在中序遍历的数组中找到前层的根节点位置
        int rootIndex = find(inorder, from, to, subNodeVal);

        // 2. 处理右子树
        if (rootIndex<to) {
            build(subNode, false, inorder, rootIndex+1, to, postorder);
        }

        // 3. 处理左子树
        if (rootIndex>from) {
            build(subNode, true, inorder, from, rootIndex-1, postorder);
        }
    }

    public TreeNode buildTree(int[] inorder, int[] postorder) {
        // 由于后续遍历的顺序是根左右，所以倒着取的时候，最先取出的是根，然后是右，最后是左，
        // 因此，本算法的整体顺序就是 : 根 -> 右 -> 左

        // 根节点先构建
        TreeNode root = new TreeNode(postorder[postorder.length-1]);

        // 特殊情况
        if(1==inorder.length) {
            return root;
        }

        // 表示从postorder的末尾取出一个元素用于构建树
        usedNum++;

        // 在中序遍历的数组中找到根节点位置
        int rootIndex = find(inorder, 0, postorder.length-1, postorder[postorder.length-1]);

        // 右子树
        if (rootIndex<(postorder.length-1)) {
            build(root, false, inorder, rootIndex + 1, inorder.length - 1, postorder);
        }

        // 左子树
        if (rootIndex>0) {
            build(root, true, inorder, 0, rootIndex - 1, postorder);
        }

        return root;
    }



    public static void main(String[] args) {
        Tools.print(new L0106().buildTree(new int[]{9,3,15,20,7}, new int[]{9,15,7,20,3})); // 3,9,20,null,null,15,7
        System.out.println("");
        Tools.print(new L0106().buildTree(new int[]{3,2,1}, new int[]{3,2,1})); // 1,2,3
    }
}
