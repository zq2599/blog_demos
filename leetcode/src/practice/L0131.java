package practice;

import java.util.*;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0131 {

    private List<List<String>> res = new ArrayList<>();

    private List<String> ans = new ArrayList<>();

//    int[][] flag = new int[16][16];

    byte[] flag = new byte[256];


    private int isPalindrome(String str, int start, int end) {
        int key = (start<<4) + end;
        /*
        if(0!=flag[start][end]) {
            return flag[start][end];
        }

        if(start>=end) {
            flag[start][end] = 1;
        } else if (str.charAt(start)==str.charAt(end)) {
            flag[start][end] = isPalindrome(str, start+1, end-1);
        } else {
            flag[start][end] = -1;
        }

        return flag[start][end];

         */

        if(0!=flag[key]) {
            return flag[key];
        }

        if(start>=end) {
            flag[key] = 1;
        } else if (str.charAt(start)==str.charAt(end)) {
            flag[key] = (byte)isPalindrome(str, start+1, end-1);
        } else {
            flag[key] = -1;
        }

        return flag[key];
    }



    private void dfs(String str, int start) {
        if (start==str.length()) {
            // 注意这里要新建一个集合实例，因为ans接下来还要用
            res.add(new ArrayList<>(ans));
        }

        for (int end=start; end<str.length(); end++) {
            // 只有符合回文的，才有必要递归
            // 用树表达的时候这样理解：start=0的情况下，end有两种取法，
            // end=1如果回文成立，就触发start=1的递归，
            // end=2如果回文成立，就触发start=2的递归，
            if(1==isPalindrome(str,start, end)) {
                ans.add(str.substring(start, end+1));
                dfs(str, end+1);
                ans.remove(ans.size()-1);
            }
        }
    }

    /**
     * 假设字符串长度为6，那么就有5个分割点，
     * 这道题就是求长度为5的boolean数组的取值全排列，
     * 每种排列都要验证是否是回文
     * @param s
     * @return
     */
    public List<List<String>> partition(String s) {

        dfs(s, 0);

        return res;
    }

    public static void main(String[] args) {

        System.out.println(new L0131().partition("aab"));
        System.out.println(new L0131().partition("a"));
        System.out.println(new L0131().partition("aa"));
        System.out.println(new L0131().partition("cdd"));
        System.out.println(new L0131().partition("ababbbabbaba"));
        System.out.println(new L0131().partition("abcdefghijk"));
//        System.out.println(1<<4);
//        System.out.println("abc".substring(0,1));
//        System.out.println(check("abaaba".toCharArray(),1, "abaaba".length()-2));


//        char[][] board = new char[][]{{'A','B','C','E'},{'S','F','C','S'},{'A','D','E','E'}};
//        System.out.println(new L0079().exist(board, "ABCCED"));
//
//
//        board = new char[][]{{'A','B','C','E'},{'S','F','C','S'},{'A','D','E','E'}};
//        System.out.println(new L0079().exist(board, "SEE"));
//
//        board = new char[][]{{'A','B','C','E'},{'S','F','C','S'},{'A','D','E','E'}};
//        System.out.println(new L0079().exist(board, "ABCB"));
    }
}
