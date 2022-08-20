package practice;

import java.util.*;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0093 {

    private List<String> res = new ArrayList<>();

    private void dfs(String s, int startIndex, int[] path, int depth) {
        String sub;
        int len;

        if (0==depth) {

        } else if (1==depth) {
            len = path[0];
            sub = s.substring(0, path[0]);


            if (len>3 || Integer.valueOf(sub)>255 || (sub.startsWith("0") && len>1)) {
                return;
            }

        } else {
            len = path[depth-1] - path[depth-2];
            sub = s.substring(path[depth-2], path[depth-1]);
            if (len>3 || Integer.valueOf(sub)>255 || (sub.startsWith("0") && len>1)) {
                return;
            }
        }

        if (3==depth) {
            len = s.length()-path[2];
            sub = s.substring(path[2]);
            if (len>3 || Integer.valueOf(sub)>255 || (sub.startsWith("0") && len>1)) {
                return;
            }

            StringBuilder sbud = new StringBuilder();

            int prev = 0;
            for(int i=0;i<3;i++) {
                sbud.append(s.substring(prev, path[i]));

                sbud.append(".");
                prev = path[i];

                if (2==i) {
                    sbud.append(s.substring(path[2], s.length()));
                }
            }

            res.add(sbud.toString());

            return;
        }


        for (int i=startIndex;i<s.length();i++) {
            path[depth] = i;

            dfs(s, i+1, path, depth+1);
        }


        return;
    }

    public List<String> restoreIpAddresses(String s) {

        // 递归
        dfs(s, 1, new int[3], 0);

        return res;
    }

    public static void main(String[] args) {
        System.out.println(new L0093().restoreIpAddresses("25525511135"));
        System.out.println(new L0093().restoreIpAddresses("101023"));
        System.out.println("***");
//        System.out.println("123".substring(2,1));

    }
}
