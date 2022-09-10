package practice;

import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0297 {

    // 本题的整体思路是前序遍历，即：根左右
    private StringBuilder serializeRes;

    private String[] deserializeArray;

    private int deserializeOffset;

    private void serializeDfs(TreeNode root) {
        if(null==root) {
//            serializeRes.append("n,");
            serializeRes.append(",");
            return;
        }

        // 1. 根
        serializeRes.append(root.val).append(",");
        // 2. 左
        serializeDfs(root.left);
        // 3. 右
        serializeDfs(root.right);
    }


    private TreeNode deserializeDfs() {
        if (deserializeOffset>=deserializeArray.length) {
            return null;
        }

//        if ("n".equals(deserializeArray[deserializeOffset])) {
        if ("".equals(deserializeArray[deserializeOffset])) {
            deserializeOffset++;
            return null;
        }

        // 1. 根
        TreeNode treeNode = new TreeNode(Integer.valueOf(deserializeArray[deserializeOffset++]));
        // 2. 左
        treeNode.left = deserializeDfs();
        // 3. 右
        treeNode.right = deserializeDfs();

        return treeNode;
    }


    // Encodes a tree to a single string.
    public String serialize(TreeNode root) {
        if (null==root) {
            return null;
        }

        serializeRes = new StringBuilder();
        serializeDfs(root);
        return serializeRes.toString();
    }

    // Decodes your encoded data to tree.
    public TreeNode deserialize(String data) {
        if (null==data) {
            return null;
        }

        deserializeArray = data.split(",");
        deserializeOffset = 0;
        return deserializeDfs();
    }



    public static void main(String[] args) {
        /*
        TreeNode t1 = new TreeNode(1);
        TreeNode t2 = new TreeNode(2);
        TreeNode t3 = new TreeNode(3);
        TreeNode t4 = new TreeNode(4);
        TreeNode t5 = new TreeNode(5);
        t1.left = t2;
        t1.right = t3;
        t3.left = t4;
        t3.right = t5;

        String str = new L0297().serialize(t1);
        System.out.println(str);
        TreeNode treeNode = new L0297().deserialize(str);
        Tools.print(treeNode);
         */

        TreeNode t1 = new TreeNode(1);
        TreeNode t2 = new TreeNode(2);
        TreeNode t3 = new TreeNode(3);
        t1.left = t2;
        t1.right = t3;
        String str = new L0297().serialize(t1);
        System.out.println(str);

        String[] array = "1,2,n,n,3,n,n,".split(",");
        System.out.println(array);

    }
}
