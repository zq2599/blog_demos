package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0268 {

    public int missingNumber(int[] nums) {
        byte[] flags = new byte[nums.length+1];

        for (int i = 0; i < nums.length; i++) {
            flags[nums[i]] = 1;
        }

        for (int i = 0; i < flags.length; i++) {
            if (0==flags[i]){
                return i;
            }
        }

        return 0;
    }

    public static void main(String[] args) {    
        System.out.println(new L0268().missingNumber(new int[]{3,0,1})); // 2
        System.out.println(new L0268().missingNumber(new int[]{0,1})); // 2
        System.out.println(new L0268().missingNumber(new int[]{9,6,4,2,3,5,7,0,1})); // 8
        System.out.println("");
    }
}
