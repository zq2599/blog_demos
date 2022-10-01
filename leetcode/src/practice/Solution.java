package practice;

import java.util.Random;

public class Solution {

    /**
     * 原始数据
     */
    private int[] raw;

    /**
     * 每一次shuffle返回的对象数组
     */
    int[] rlt;

    /**
     * 记录所有元素的使用情况（数组下标的值对应raw的元素的数组下标），一旦用掉，就给其设置标志
     */
    byte[] flags;

    private Random r = new Random();

    public Solution(int[] nums) {
        raw = nums;
        rlt = new int[nums.length];
        flags = new byte[nums.length];
    }

    public int[] reset() {
        return raw;
    }

    /**
     * 从数组中选一个可用数字返回，并且将该数字设置为已用
     * @param avaliableNum
     * @param flags
     * @return
     */
    private int randomGet(int avaliableNum, byte[] flags) {
        int sequence = 1==avaliableNum ? 0 : r.nextInt(avaliableNum);
//        System.out.println("total [" + avaliableNum + "], random get [" + sequence + "]");

        int passNum = 0;

        // 假设sequence等于3，接下来就要从flags中找到第3个可用的元素，并将其设置为已用
        for (int i = 0; i < flags.length; i++) {

            if (0!=flags[i]) {
                continue;
            }

            if (passNum++==sequence) {
                // 设置为可用
                flags[i] = 1;
                // 返回数组下标
                return i;
            }
        }

        // 正常情况下，不会走到这里
        return 0;
    }

    public int[] shuffle() {
        for (int i=0;i<raw.length;i++) {
            flags[i] = 0;
        }

        for (int i=0;i<raw.length;i++) {
            rlt[i] = raw[randomGet(raw.length-i, flags)];
        }

        return rlt;
    }

    public static void main(String[] args) {
        int[] array = new int[] {1, 2, 3};

        Solution solution = new Solution(array);
        Tools.print(solution.shuffle());
        Tools.print(solution.reset());
        Tools.print(solution.shuffle());
    }
}
