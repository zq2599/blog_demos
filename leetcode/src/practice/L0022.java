package practice;

import java.util.LinkedList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0022 {

    List<String> res = new LinkedList<>();

    // 首先，这不是全排列，不论把第几个左括号放在最前面都是同一个效果！
    // dfs深度等于2n
    // 左括号数量只要小于n，就可以添加，
    // 右括号数量只要小于左括号，就可以添加
    public List<String> generateParenthesis(int n) {
        // 按照题目要求，n对括号，所以长度固定是2n
        char[] path = new char[2*n];
        dfs(path, 0);
        return res;
    }

    private void dfs(char[] path, int depth) {
        // 终止条件，搜集结果
        if(depth==path.length) {
            res.add(new String(path));
            return;
        }

        int leftNum = 0;

        // 统计左括号的数量
        for(int i=0;i<depth;i++) {
            if('('==path[i]) {
                leftNum++;
            }
        }

        // 如果当前的左括号数量没有超过最大数量，那就可以一直添加下去
        if (leftNum<path.length/2) {
            path[depth] = '(';
            dfs(path, depth+1);
        }

        // 注意，这里没有常规的回溯代码那样的remove等操作，而是像前面那样继续对path[depth]这个元素赋值，并且调用dfs的时候，深度依旧是depth+1,
        // 代码写到这里，似乎对回溯的理解更深刻了：对path的删除只是回溯操作的手段之一，真正的回溯，是指将处理牢牢控制在树的当前层次！
        // 注意，不要受上面的dfs代码影响，这是第depth层，path中有效内容的长度依旧是depth！
        // 第depth层，path有效内容的长度就是depth，所以右括号的数量等于depth-leftNum
        if ((depth-leftNum)<leftNum) {
            path[depth] = ')';
            dfs(path, depth+1);
        }
    }

    public static void main(String[] args) {
        Tools.printStr(new L0022().generateParenthesis(2));
    }
}
