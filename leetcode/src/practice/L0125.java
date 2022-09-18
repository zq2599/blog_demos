package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0125 {

    public boolean isPalindrome(String s) {




        // 特殊情况处理
        if (s.length()<1) {
            return true;
        }

        char[] array = s.toCharArray();

        int left = 0;
        int right = array.length-1;

        while (left<right) {
            // 题目不区分大小写，这里转为大写
            if (array[left]>='a' && array[left]<='z') {
                array[left] -= 32;
            }

            // 如果不在字母范围内，就跳到下一个
            if ( !(array[left]>='0' && array[left]<='9')
              && !(array[left]>='A' && array[left]<='Z')) {
                left++;
                continue;
            }

            if (array[right]>='a' && array[right]<='z') {
                array[right] -= 32;
            }

            // 如果不在字母范围内，就跳到下一个
            if ( !(array[right]>='0' && array[right]<='9')
              && !(array[right]>='A' && array[right]<='Z')) {
                right--;
                continue;
            }

            if (array[left++]==array[right--]) {
                continue;
            } else {
                return false;
            }

        }

        return true;

    }


    public static void main(String[] args) {


        System.out.println(new L0125().isPalindrome("a1a"));
    }
}
