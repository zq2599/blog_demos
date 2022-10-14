package practice;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @program: leetcode
 * @description:
 * @author: zq2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0449 {
    Map<String,TreeNode> map = new HashMap<>();

    /**
     * 二叉搜索树十分适合用后序遍历做序列化&反序列化，因为根节点在最后，定位和处理都方便
     * @param root
     * @param list
     */
    private void postOrder(TreeNode root, List<Integer> list) {
        if (null==root) {
            return;
        }

        // 1. 左
        postOrder(root.left, list);
        // 2. 右
        postOrder(root.right, list);
        // 3. 根
        list.add(root.val);
    }

    public String serialize(TreeNode root) {
        if (null==root) {
            return null;
        }

        List<Integer> list = new ArrayList<>();

        postOrder(root, list);

        StringBuilder sbud = new StringBuilder();

        for (int i=0;i<list.size()-1;i++) {
            sbud.append(list.get(i));
            sbud.append(",");
        }

        sbud.append(list.get(list.size()-1));

        return sbud.toString();
    }

    // Decodes your encoded data to tree.
    public TreeNode deserialize(String data) {
        if (null==data) {
            return null;
        }

        String[] strArray = data.split(",");

        if (strArray.length<1) {
            return null;
        }

        int[] array = new int[strArray.length];

        // 全部转成int，后面好处理
        for (int i = 0; i < array.length; i++) {
            array[i] = Integer.parseInt(strArray[i]);
        }

        TreeNode root = new TreeNode(array[array.length-1]);

        // 处理每一段：找出根节点的左右节点后，再处理左右子树
        construct(array, 0, array.length-2, root);
        return root;   
    }

    /**
     * 递归处理每一段
     * @param array
     * @param start
     * @param end
     * @param root
     */
    private void construct(int[] array, int start, int end, TreeNode root) {
        int leftStart = start;
        int leftEnd = end;
        int rightStart = start;
        int rightEnd = end;
        
        // 是否找到了右子树
        boolean find = false;

        for (int i = start; i <= end; i++) {
            // 找到第一个大于根节点的元素时，从此位置到end，都是元素的右子节点
            if (array[i]>root.val) {
                leftEnd = i-1;
                rightStart = i;
                find = true;
                // 注意，只找第一个！
                break;
            }
        }

        // 如果存在左子树，最后一个节点就是左子树的根节点
        if (leftEnd>=start) {
            TreeNode left = new TreeNode(array[leftEnd]);
            root.left = left;

            // 是个树，就要构建
            if (leftEnd>leftStart) {
                construct(array, leftStart, leftEnd-1, left);
            }
        }

        // 如果存在右子树
        if (find) {
            TreeNode right = new TreeNode(array[rightEnd]);
            root.right = right;

            // 是个树，就要构建
            if (rightEnd>rightStart) {
                construct(array, rightStart, rightEnd-1, right);
            }
        }
    }

   
    public static void main(String[] args) {
        
        TreeNode t1 = new TreeNode(3);
        TreeNode t2 = new TreeNode(1);
        TreeNode t3 = new TreeNode(4);
        TreeNode t4 = new TreeNode(2);


        t1.left = t2;
        t1.right = t3;
        t2.right = t4;

        L0449 l = new L0449();
        String a = l.serialize(t1);
        System.out.println(a);

        TreeNode tx = l.deserialize(a);
        System.out.println(l.serialize(tx));
        
        System.out.println("");
    }
}
