package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L1608 {

    public int specialArray(int[] nums) {

        int max = Integer.MIN_VALUE;
        int xLimit;
        int[] buckets = new int[1001];

        for(int i=0;i< nums.length;i++) {
            max = Math.max(max, nums[i]);
            buckets[nums[i]]++;
        }

        // 根据命题数量肯定不会超过数组长度
        xLimit = Math.min(max, nums.length);

        for (int i=1;i<=xLimit;i++) {
            if (check(buckets, max, i)) {
                return i;
            }
        }

        return -1;
    }

    private boolean check(int[] buckets, int max, int x) {
        int count = 0;

        for(int i=x;i<=max;i++) {
            count += buckets[i];

            if (count>x) {
                return false;
            }
        }

        return count==x;
    }

    public static void main(String[] args) {
        System.out.println(new L1608().specialArray(new int[]{3,5})); // 2
        System.out.println(new L1608().specialArray(new int[]{0,0})); // -1
        System.out.println(new L1608().specialArray(new int[]{0,4,3,0,4})); // 3
        System.out.println(new L1608().specialArray(new int[]{3,6,7,7,0})); // -1
    }
}
