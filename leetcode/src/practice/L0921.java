package practice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0921 {

    public int minAddToMakeValid(String s) {
        char[] array = s.toCharArray();

        // 当前栈中左括号的数量
        int stackLeftNum = 0;

        // 需要补充的左括号数量
        int addLeftNum = 0;

        for (int i = 0; i < array.length; i++) {
            if ('(' == array[i]) {
                stackLeftNum++;
            } else {
                // 遇到右括号的时候，如果当前栈里还有左括号，就消耗掉一个，
                // 如果当前栈里没有左括号，就要额外添加了，记录到addLeftNum中
                if (stackLeftNum>0) {
                    stackLeftNum--;
                } else {
                    addLeftNum++;
                }
            }
        }

        return stackLeftNum + addLeftNum;
    }

    public static void main(String[] args) {
        System.out.println(new L0921().minAddToMakeValid("())")); // 1
        System.out.println("");
        System.out.println(new L0921().minAddToMakeValid("(((")); // 3
        System.out.println("");
    }
}
