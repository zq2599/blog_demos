package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0306 {

    boolean res = false;

    private static boolean check(String num, int pos0, int pos1, int pos2, int pos3) {

        // 加数长度大于和是不可能的
        if ((pos1-pos0)>(pos3-pos2)) {
            return false;
        }

        if ((pos2-pos1)>(pos3-pos2)) {
            return false;
        }

        String add1Str = num.substring(pos0, pos1);
        String add2Str = num.substring(pos1, pos2);

        StringBuilder sb = new StringBuilder();

        int i = add1Str.length() - 1, j = add2Str.length() - 1;
        int upper = 0;

        while (i >= 0 || j >= 0) {
            int a = 0, b = 0;
            if (i >= 0) a = add1Str.charAt(i) - '0';
            if (j >= 0) b = add2Str.charAt(j) - '0';
            int sum = a + b + upper;
            upper = sum / 10;
            sb.append(sum % 10);
            i--;
            j--;
        }

        if (upper != 0) {
            sb.append(upper);
        }

        String addRlt = sb.reverse().toString();

        return addRlt.equals(num.substring(pos2, pos3));
    }

    private static boolean isZeroStart(String num, int start, int end) {
        String lastPart = num.substring(start, end);
        return (lastPart.length()>1 && '0'==lastPart.charAt(0));
    }

    private void dfs(String num, int startIndex, ArrayList<Integer> path) {
        int size = path.size();

        if(2==size) {
            if (!isZeroStart(num, path.get(1), num.length())
            && check(num, 0, path.get(0), path.get(1), num.length())) {
                res = true;
                return;
            }
        }
        else if(size>2) {
            int pos0 = size>3 ? path.get(size-4) : 0;
            int pos1 = path.get(size-3);
            int pos2 = path.get(size-2);
            int pos3 = path.get(size-1);

            // 如果不符合规则，就终止接下来的递归
            if (!check(num, pos0, pos1, pos2, pos3)) {
                return;
            }

            // 剩下的所有内容作为结果，验证一下是否满足需求
            // 满足的话，就表示整串都满足了，可以返回，因为题目要求返回true或者false

            // 不允许出现以0开头的多位数字，所以，剩下的部分如果是以0开头的多位数字，就不需要转成数字了（还要继续分割）
            if (!isZeroStart(num, pos3, num.length())
                    && check(num, pos1, pos2, pos3, num.length())) {
                res = true;
                return;
            }

            /*
            System.out.println("path : " + path);
            int rlt = Integer.valueOf(num.substring(path.get(size-2), path.get(size-1)));
            int add2 = Integer.valueOf(num.substring(path.get(size-3), path.get(size-2)));

            int start = size>3 ? path.get(size-4) : 0;
            int add1 = Integer.valueOf(num.substring(start, path.get(size-3)));
            System.out.println("add1 [" + add1 + "], add2 [" + add2 + "], rlt [" + rlt + "]");

            if(rlt!=(add1+add2)) {
                return;
            }

            // 用剩下的内容做一下检查，看看如果相等，就可以提前结束了
            int last = Integer.valueOf(num.substring(path.get(size-1), num.length()));

            System.out.println("last [" + last + "]");

            if (last==(rlt+add2)) {
                res = true;
                return;
            }
            */
        }

        for(int i=startIndex;i<num.length();i++) {
//            System.out.println("i : [" + i + "], val : " + num.charAt(i));
//            if('0'==num.charAt(i)) {
//                continue;
//            }

            size = path.size();

            String sub = "";

            if (size>0) {
                sub = num.substring(path.get(size-1), i);
            } else {
                sub = num.substring(0,i);
            }

            if (sub.length()>1 && '0'==sub.charAt(0)) {
//                System.out.println(sub);
                continue;
            }



            path.add(i);
            dfs(num, i+1, path);
            // 已经匹配成功，就可以提前返回了
            if (res) {
                return;
            }
            path.remove(path.size()-1);
        }
    }

    public boolean isAdditiveNumber(String num) {
        dfs(num, 1, new ArrayList<>());
        return res;
    }

    public static void main(String[] args) {
//        System.out.println((new L0306().isAdditiveNumber("112358")));
//        System.out.println((new L0306().isAdditiveNumber("199100199")));
//        System.out.println((new L0306().isAdditiveNumber("1023")));
        // 预期是true
        System.out.println((new L0306().isAdditiveNumber("101")));

        // 预期 false
        System.out.println((new L0306().isAdditiveNumber("1203")));

//        System.out.println((new L0306().isAdditiveNumber("198019823962")));
        // 预期 true
        System.out.println((new L0306().isAdditiveNumber("121474836472147483648")));



//        System.out.println("1234".substring(0,1));
//        System.out.println("1234".substring(1,2));
//        System.out.println("1234".substring(2,3));
    }

}
