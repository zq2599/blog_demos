package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0788 {

    /*
    public int rotatedDigits(int n) {
        int num = 0;
        char[] array;
        int val;
        boolean validFlag;

        for (int i=1;i<=n;i++) {

            validFlag = false;

            array = String.valueOf(i).toCharArray();

            // 不能有3,4,7
            // 2,5,6,9至少出现一次
            for (int j = 0; j < array.length; j++) {
                val = array[j]-'0';

                // 3,4,7一旦出现，表示该数字无法旋转了，退出当前数字的判断
                if (3==val || 4==val || 7==val) {
                    validFlag = false;
                    break;
                }

                // 2,5,6,9只要出现一次，就表示该数字可以旋转
                if (2==val || 5==val || 6==val || 9==val) {
                    validFlag = true;
                }
            }

            if (validFlag) {
                num++;
            }
        }

        return num;
    }
    */

    // 0:无所谓的数字，有0,1,8
    // 1:必须要出现一次的数字，有2,5,6,9
    // 2:不允许出现的数字，有3,4,7

    private static final int[] types = new int[] {
            0,   // 0
            0,   // 1
            1,   // 2
            2,   // 3
            2,   // 4
            1,   // 5
            1,   // 6
            2,   // 7
            0,   // 8
            1    // 9
            };

    public int rotatedDigits(int n) {
        int num = 0;

        // 当前数字的类型
        int type;
        // 当前数字最低位的类型
        int singleNumberType;

        // 当前数字的值(会不停地缩小)
        int val;

        for (int i=1;i<=n;i++) {
            type = 0;

            val = i;

            // 每次缩小10倍，再检查个位
            while (val>0) {
                singleNumberType = types[val % 10];

                if (2==singleNumberType) {
                    type = 2;
                    break;
                }

                if (1==singleNumberType) {
                    type = 1;
                }

                val /= 10;
            }

            if (1==type) {
                num++;
            }
        }

        return num;
    }




    public static void main(String[] args) {
        System.out.println(new L0788().rotatedDigits(10)); // 4
        System.out.println(new L0788().rotatedDigits(2)); // 1
        System.out.println(new L0788().rotatedDigits(857)); // 247
    }
}
