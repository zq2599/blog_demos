package practice;

import java.util.Deque;
import java.util.LinkedList;

/**
 * @program: leetcode
 * @description: 快排
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0020 {

    public boolean isValid(String s) {
        Deque<Character> stack = new LinkedList<>();

        char pop;

        for (char c: s.toCharArray()) {
            if ('('==c || '{'==c || '['==c) {
                stack.push(c);
            } else {
                if(stack.isEmpty()) {
                    return false;
                }

                pop = stack.pop();

                if (')'==c && '('!=pop) {
                    return false;
                }

                if ('}'==c && '{'!=pop) {
                    return false;
                }

                if (']'==c && '['!=pop) {
                    return false;
                }
            }
        }

        return stack.isEmpty();
    }


    public static void main(String[] args) {

        System.out.println(new L0020().isValid("(]"));
    }
}
