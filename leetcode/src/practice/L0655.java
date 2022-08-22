package practice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0655 {

    private int height = 0;

    String[][] array = null;

    private void treeHeight(TreeNode treeNode, int depth) {
        if (null==treeNode) {
            return;
        }

        // 非空的话，高度加一
        if (depth>height) {
            height = depth;
        }

        treeHeight(treeNode.left, depth +1);
        treeHeight(treeNode.right, depth +1);
    }

    private void print(TreeNode treeNode, boolean isLeft, int parentRow, int parentColumn) {
        if (null==treeNode) {
            return;
        }

        int myRow = parentRow+1;
        int myColumn = isLeft
                     ? (parentColumn-(int)Math.pow(2, height-parentRow-1))
                     : (parentColumn+(int)Math.pow(2, height-parentRow-1));

        array[myRow][myColumn] = String.valueOf(treeNode.val);

        print(treeNode.left,true, myRow, myColumn);
        print(treeNode.right,false, myRow, myColumn);
    }


    public List<List<String>> printTree(TreeNode root) {
        // 求出高度来
        treeHeight(root, 0);

        // 初始化矩阵
        array = new String[height+1][(int)Math.pow(2, height+1)-1];

        for(int i=0;i<array.length;i++) {
            Arrays.fill(array[i], "");
        }

        int myRow = 0;
        int myColumn = (array[0].length-1)/2;

        array[myRow][myColumn] = String.valueOf(root.val);

        print(root.left,true, myRow, myColumn);
        print(root.right,false, myRow, myColumn);

        List<List<String>> rlt = new ArrayList<>();

        for(int i=0;i<array.length;i++) {
            rlt.add(Arrays.asList(array[i]));
        }

        return rlt;
    }



    public static void main(String[] args) {
        TreeNode t1 = new TreeNode(1);
        TreeNode t2 = new TreeNode(2);
        TreeNode t3 = new TreeNode(3);
        TreeNode t5 = new TreeNode(4);

        t1.left = t2;
        t1.right = t3;
        t2.right = t5;

        System.out.println(new L0655().printTree(t1));

        /*
        TreeNode t1 = new TreeNode(1);
        TreeNode t2 = new TreeNode(2);
        t1.left = t2;
        System.out.println(new L0655().printTree(t1));
         */
    }
}
