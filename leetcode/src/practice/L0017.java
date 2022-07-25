package practice;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0017 {

    List<String> res = new LinkedList<>();

    private final static char[][] letters = {{'a', 'b', 'c'},
            {'d', 'e', 'f'},
            {'g', 'h', 'i'},
            {'j', 'k', 'l'},
            {'m', 'n', 'o'},
            {'p', 'q', 'r', 's'},
            {'t', 'u', 'v'},
            {'w', 'x', 'y', 'z'}
    };

    // 注意这里和全排列的区别，全排列每次dfs的for循环都是相通的数字，所以才需要一个used数组，表示那些已经使用过，
    // 这里每次dfs的for循环内容都是不同的，所以不需要used数组
    private void dfs(char[] digits, char[] path, int depth) {
        // 终止，搜集
        if (digits.length==depth) {
            res.add(new String(path));
            return;
        }

        // 假设当前是第0层，那么就应该取letters中的第0个数组，作为子节点展开
        char[] currentLayerArray = letters[digits[depth]-'2'];

        // 处理每个元素
        for (int i=0;i<currentLayerArray.length;i++) {
            path[depth] =currentLayerArray[i];
            dfs(digits, path, depth+1);
        }
    }

    public List<String> letterCombinations(String digits) {
        // 注意审题，digits的长度可能为0
        if (digits.length()<1) {
            return new LinkedList<>();
        }

        char[] digitsArray = digits.toCharArray();

        char[] path = new char[digitsArray.length];

        dfs(digitsArray, path, 0);

        return res;
    }



    public static void main(String[] args) {
        System.out.println(new L0017().letterCombinations("23"));
    }
}
