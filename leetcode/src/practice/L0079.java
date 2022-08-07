package practice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0079 {

    public boolean exist(char[][] board, String word) {
        return dfs(board,
                new boolean[board.length][board[0].length],
                word.toCharArray(),
                0,
                0,
                0);
    }

    private boolean dfs(char[][]board, boolean[][] path, char[] words, int depth, int srcRow, int srcCol) {
        boolean rlt;
        // 如果深度为0，表示刚开始，这时候寻找范围是没有限制的
        if (0==depth) {
           int totalLen = board.length * board[0].length;

           if(words.length>totalLen) {
               return false;
           }

           if(1== words.length && 1==totalLen) {
               return words[0]==board[0][0];
           }

           for(int i=0;i<board.length;i++) {
              for(int j=0;j<board[0].length;j++) {
                  // 要匹配第一个字符才有必要继续
                  if (board[i][j]==words[0]) {

                      if(1==words.length) {
                          return true;
                      }

                      path[i][j] = true;
                      rlt = dfs(board, path, words,2, i, j);

                      if(rlt) {
                          return true;
                      }

                      path[i][j] = false;
                  }
              }
           }

           // 从最顶层遍历完了，还没有提前返回true，就证明没有找到，返回false
           return false;

        } else {

            // 按照东南西北的顺序搜索

            // 东：行不变，列加一
            if(srcCol< (board[0].length-1)) {
                if(!path[srcRow][srcCol+1] && board[srcRow][srcCol+1]==words[depth-1]) {

                    // 如果是最后一层，找到了，就要返回true
                    if (depth== words.length) {
                        return true;
                    }

                    path[srcRow][srcCol+1] = true;

                    rlt = dfs(board, path, words, depth+1, srcRow, srcCol+1);
                    // 如果已经找到，就立即返回啦
                    if(rlt) {
                        return true;
                    }

                    path[srcRow][srcCol+1] = false;
                }
            }

            // 南：行加一，列不变
            if(srcRow< (board.length-1)) {
                if(!path[srcRow+1][srcCol] && board[srcRow+1][srcCol]==words[depth-1]) {

                    // 如果是最后一层，找到了，就要返回true
                    if (depth== words.length) {
                        return true;
                    }

                    path[srcRow+1][srcCol] = true;

                    rlt = dfs(board, path, words, depth+1, srcRow+1, srcCol);
                    // 如果已经找到，就立即返回啦
                    if(rlt) {
                        return true;
                    }

                    path[srcRow+1][srcCol] = false;
                }
            }

            // 西：行不变，列减一
            if(srcCol>0) {
                if(!path[srcRow][srcCol-1] && board[srcRow][srcCol-1]==words[depth-1]) {

                    // 如果是最后一层，找到了，就要返回true
                    if (depth== words.length) {
                        return true;
                    }

                    path[srcRow][srcCol-1] = true;

                    rlt = dfs(board, path, words, depth+1, srcRow, srcCol-1);
                    // 如果已经找到，就立即返回啦
                    if(rlt) {
                        return true;
                    }

                    path[srcRow][srcCol-1] = false;
                }
            }


            // 北：行减一，列不变
            if(srcRow>0) {
                if(!path[srcRow-1][srcCol] && board[srcRow-1][srcCol]==words[depth-1]) {

                    // 如果是最后一层，找到了，就要返回true
                    if (depth== words.length) {
                        return true;
                    }

                    path[srcRow-1][srcCol] = true;

                    rlt = dfs(board, path, words, depth+1, srcRow-1, srcCol);
                    // 如果已经找到，就立即返回啦
                    if(rlt) {
                        return true;
                    }

                    path[srcRow-1][srcCol] = false;
                }
            }
        }

        // 东南西北都没有找到，就只能返回了
        return false;
    }



    public static void main(String[] args) {

        char[][] board = new char[][]{{'a','a'}};
        System.out.println(new L0079().exist(board, "a"));


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
