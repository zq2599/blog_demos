package practice;

import java.util.List;

/**
 * @program: leetcode
 * @description: 静态工具
 * @author: za2599@gmail.com
 * @create: 2022-07-03 08:35
 **/
public class Tools {

    /**
     * 递归打印联表
     * @param listNode
     */
    public static void print(ListNode listNode) {
        if(null==listNode) {
            System.out.println("listNode is null");
            return;
        }

        System.out.print(listNode.val);

        if (null==listNode.next) {
            System.out.println("");
            return;
        }

        System.out.print(", ");
        print(listNode.next);
    }

    /**
     * 构建链表
     * @param vals
     */
    public static ListNode buildListNode(int...vals) {
        ListNode listNode = new ListNode();
        ListNode head = listNode;

        for(int val:vals) {
            listNode.next = new ListNode(val);
            listNode = listNode.next;
        }

        return head.next;
    }

    public static void print(int[] array) {
        for(int i=0;i<array.length;i++) {
            System.out.print(array[i]);
            if((array.length-1)!=i){
                System.out.print(", ");
            }else {
                System.out.print("\n");
            }
        }
    }

    public static void print(char[] array) {
        for(int i=0;i<array.length;i++) {
            System.out.print(array[i]);
            if((array.length-1)!=i){
                System.out.print(", ");
            }
        }
    }

    public static void print(int[][] array) {
        for(int i=0;i<array.length;i++) {

            for (int j=0;j<array.length;j++) {
                System.out.print(array[i][j]);
                if((array.length-1)!=j){
                    System.out.print(", ");
                } else {
                    System.out.print("\n");
                }
            }
        }
    }


    public static void print(String prefix, int[] array) {
        System.out.print(prefix + " [");
        for(int i=0;i<array.length;i++) {
            System.out.print(array[i]);
            if((array.length-1)!=i){
                System.out.print(", ");
            }
        }
        System.out.print("]\n");
    }


    public static void print(List<Integer> list) {
        for (Integer val : list) {
            System.out.println(val);
        }
    }

    public static void printStr(List<String> list) {
        for (String val : list) {
            System.out.println(val);
        }
    }

    public static void printOneLine(List<Integer> list) {
        for (int i=0;i<list.size();i++) {
            System.out.print(list.get(i));
            if (i<list.size()-1) {
                System.out.print(", ");
            }
        }
        System.out.print("\n");
    }

    public static void printOneLine(String prefix, List<Integer> list) {
        System.out.print(prefix + " ");
        for (int i=0;i<list.size();i++) {
            System.out.print(list.get(i));
            if (i<list.size()-1) {
                System.out.print(", ");
            }
        }
        System.out.print("\n");
    }

    public static void print(TreeNode treeNode) {
        if(null==treeNode) {
            System.out.println("treeNode is null");
            return;
        }

        System.out.print(treeNode.val);

        System.out.print(", ");

        if (null!=treeNode.left) {
            print(treeNode.left);
        }

        if (null!=treeNode.right) {
            print(treeNode.right);
        }
    }
}
