package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0108 {

    public TreeNode sortedArrayToBST(int[] nums) {
        if (nums.length<1) {
            return null;
        }

        return buildBST(nums, 0, nums.length-1);
    }

    /**
     * 递归处理
     * @param nums
     * @param start
     * @param end
     * @return
     */
    private TreeNode buildBST(int[] nums, int start, int end) {
        // 终止条件是数组处理完毕
        if(start>end) {
            return null;
        }

        int middle = (end+start)/2;

        TreeNode root = new TreeNode(nums[middle]);

        root.left = buildBST(nums, start, middle -1);
        root.right = buildBST(nums, middle+1, end);

        return root;
    }





    public static void main(String[] args) {
        System.out.println(new L0108().sortedArrayToBST(new int[] {-10,-3,0}));
    }
}
