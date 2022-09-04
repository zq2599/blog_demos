package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0105 {

    private TreeNode allRoot;

    private int usedNum = 0;

    public TreeNode buildTree(int[] preorder, int[] inorder) {
        build(null, true, preorder, 0, preorder.length-1, inorder);
        return allRoot;
    }


    // 1. 从数组中取出根节点
    // 2. 放入父节点的左或者右子节点
    // 3. 在中序数组中找到位置，然后分割为左右两段



    private void build(TreeNode root, boolean isLeft, int[] preorder, int from, int to, int[] inorder) {

        // 1. 从数组中取出根节点
        int currentLevelRootVal = preorder[usedNum];

        TreeNode newNode = new TreeNode(currentLevelRootVal);

        if (null==root) {
            allRoot = newNode;
        } else {
            // 2. 放入父节点的左或者右子节点
            if (isLeft) {
                root.left = newNode;
            } else {
                root.right = newNode;
            }
        }

        usedNum++;

        if (from==to) {
            return;
        }

        int rootIndex = 0;

        // 3. 在中序数组中找到位置，然后分割为左右两段
        for(int i=from;i<=to;i++) {
            if (currentLevelRootVal==inorder[i]) {
                rootIndex = i;
                break;
            }
        }

        if (rootIndex>from) {
            build(newNode, true, preorder, from, rootIndex-1, inorder);
        }

        if (rootIndex<to) {
            build(newNode, false, preorder, rootIndex+1, to, inorder);
        }

    }

    public static void main(String[] args) {
        Tools.print(new L0105().buildTree(new int[]{3,9,20,15,7}, new int[]{9,3,15,20,7})); // 3,9,20,null,null,15,7
    }
}
