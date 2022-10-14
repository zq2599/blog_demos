package offer;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0033 {

    private boolean verify(int[] array, int start, int end) {
        // System.out.println("开始验证[" + start + "]-[" + end + "]，总长[" + array.length + "]");

        // 终止条件
        if (start >= end) {
            return true;
        }

        int leftStart = start;
        // 注意，leftEnd的默认值必须是end-1，因为一旦不存在右子树，整段都是左子树
        int leftEnd = end-1;

        int rightStart = start;
        int rightEnd = end - 1;
        
        boolean find = false;
        // 先划分左右子树
        for (int i = start; i <= end - 1; i++) {
            // System.out.println("i=[" + i + "], array[i]=[" + array[i] + "], array[end]=" + array[end] + "]");
            // 第一个比end大的元素，就是右子树
            if (array[i] > array[end]) {
                find = true;
                // System.out.println("i=[" + i + "]");
                leftEnd = i - 1;
                rightStart = i;
                break;
            }
        }

        // System.out.println("左[" + leftStart + "]-[" + leftEnd + "]，右[" + rightStart + "]-[" + rightEnd + "]");

        // 左子树都要比根节点小
        for (int i = leftStart; i <= leftEnd; i++) {
            if (array[i] > array[end]) {
                // System.out.println("------1");
                return false;
            }
        }

        // 确实存在右子树
        if (find) {
            // 右子树都要比自己大
            for (int i = rightStart; i <= rightEnd; i++) {
                if (array[i] < array[end]) {
                    // System.out.println("------2");
                    return false;
                }
            }
        }

        // 递归校验左子树
        if (!verify(array, leftStart, leftEnd)) {
            // System.out.println("------3");
            return false;
        }

        // 递归的校验右子树
        return find ? verify(array, rightStart, rightEnd) : true;
    }

    public boolean verifyPostorder(int[] postorder) {
        return verify(postorder, 0, postorder.length - 1);
    }

    public static void main(String[] args) {
        // System.out.println(new L0033().verifyPostorder(new int[]{1,6,3,2,5})); //
        // false
        // System.out.println("");

        // System.out.println(new L0033().verifyPostorder(new int[]{1,3,2,6,5})); //
        // false
        // System.out.println("");

        // System.out.println(new L0033().verifyPostorder(new int[] { 4, 6, 7, 5 })); // true
        // System.out.println("");

        System.out.println(new L0033().verifyPostorder(new int[] { 5, 2, -17, -11, 25, 76, 62, 98, 92, 61 })); // true
        System.out.println("");
    }
}
