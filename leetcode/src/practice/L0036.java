package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0036 {

    public boolean isValidSudoku(char[][] board) {
        // 行
        byte[][] rowFlags = new byte[9][9];

        // 列
        byte[][] colFlags = new byte[9][9];

        // 九宫
        byte[][] matrixFlags = new byte[9][9];

        int offset;
        int matrixOffset;

        for (int i= 0;i<board.length;i++) {
            for (int j=0;j<board[0].length;j++) {
                if ('.'==board[i][j]) {
                    continue;
                }

                offset = board[i][j] - '1';

                // 1. 先检查行
                // 当前的数字如果在同一行出现过，就返回false
                if (1==rowFlags[i][offset]) {
                    return false;
                }

                // 设置为true，表示已经出现过了
                rowFlags[i][offset] = 1;

                // 2. 再检查列
                // 当前的数字如果在同一列出现过，就返回false
                if (1==colFlags[j][offset]) {
                    return false;
                }

                // 设置为true，表示在第j列已经出现过了
                colFlags[j][offset] = 1;

                // 3. 检查九宫
                switch(i) {
                    case 0:
                    case 1:
                    case 2:
                        if (j<3){
                            matrixOffset = 0;
                        } else if (j<6) {
                            matrixOffset = 1;
                        } else {
                            matrixOffset = 2;
                        }

                        break;
                    case 3:
                    case 4:
                    case 5:
                        if (j<3){
                            matrixOffset = 3;
                        } else if (j<6) {
                            matrixOffset = 4;
                        } else {
                            matrixOffset = 5;
                        }

                        break;
                    default:
                        if (j<3){
                            matrixOffset = 6;
                        } else if (j<6) {
                            matrixOffset = 7;
                        } else {
                            matrixOffset = 8;
                        }
                        break;
                }

                // 当前的数字如果在同一列九宫出现过，就返回false
                if (1==matrixFlags[matrixOffset][offset]) {
                    return false;
                }

                // 设置为true，表示在第j列已经出现过了
                matrixFlags[matrixOffset][offset] = 1;
            }
        }

        return true;
    }




    public static void main(String[] args) {
        char[][] board = new char[][]{
                {'5','3','.','.','7','.','.','.','.'}
                ,{'6','.','.','1','9','5','.','.','.'}
                ,{'.','9','8','.','.','.','.','6','.'}
                ,{'8','.','.','.','6','.','.','.','3'}
                ,{'4','.','.','8','.','3','.','.','1'}
                ,{'7','.','.','.','2','.','.','.','6'}
                ,{'.','6','.','.','.','.','2','8','.'}
                ,{'.','.','.','4','1','9','.','.','5'}
                ,{'.','.','.','.','8','.','.','7','9'}};

        System.out.println(new L0036().isValidSudoku(board));

        board = new char[][]{
                {'8','3','.','.','7','.','.','.','.'}
                ,{'6','.','.','1','9','5','.','.','.'}
                ,{'.','9','8','.','.','.','.','6','.'}
                ,{'8','.','.','.','6','.','.','.','3'}
                ,{'4','.','.','8','.','3','.','.','1'}
                ,{'7','.','.','.','2','.','.','.','6'}
                ,{'.','6','.','.','.','.','2','8','.'}
                ,{'.','.','.','4','1','9','.','.','5'}
                ,{'.','.','.','.','8','.','.','7','9'}};

        System.out.println(new L0036().isValidSudoku(board));


    }
}
