package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L1313 {

    public int[] decompressRLElist(int[] nums) {
        int[] array = new int[nums.length*50];
        int len = 0;

        for (int i=0;i<nums.length;i+=2){
            for (int j = 0; j < nums[i]; j++) {
                array[len+j] = nums[i+1];
            }
            len+=nums[i];
        }

        int[] rlt = new int[len];

        System.arraycopy(array, 0, rlt, 0, len);

        return rlt;
    }

    public static void main(String[] args) {
        Tools.print(new L1313().decompressRLElist(new int[]{1,2,3,4}));  // 2,4,4,4
        Tools.print(new L1313().decompressRLElist(new int[]{1,1,2,3}));  // 1,3,3

        System.out.println("");
    }

}
