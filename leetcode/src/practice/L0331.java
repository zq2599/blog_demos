package practice;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0331 {

    // 9,3,4,#,#,1,#,#,2,#,6,#,#
    /*
    public boolean isValidSerialization(String preorder) {
        String[] array = preorder.split(",");

        Deque<String> deque = new LinkedList<String>();
        Deque<String> temp = new LinkedList<String>();

        // 入栈
        for (int i=0;i<array.length;i++) {
            deque.offerLast(array[i]);
        }

        String lastLast = null;;
        String last = null;

        while (!deque.isEmpty()) {

            String current = deque.pollFirst();
            // System.out.println("1. 取出了["+current+"]");

            // 凑不齐三个就不用检查
            if (null==lastLast) {
                // System.out.println("2. 凑不齐三个");
                temp.offerLast(current);
                lastLast = last;
                last = current;
                continue;
            }


            // System.out.println("3. [" + lastLast + "]-[" + last + "]-[" + current + "]");

            if (!"#".equals(lastLast) && "#".equals(last) && "#".equals(current)) {
                // System.out.println("4. [" + lastLast + "]-[" + last + "]-[" + current + "]，这是个叶子结点，处理后将temp全部塞回去");
                // 注意：刚才是从头顶取的，要从头顶塞回去
                deque.offerFirst(current);

                // temp要丢掉两个
                temp.pollLast();
                temp.pollLast();

                while(!temp.isEmpty()) {
                    deque.offerFirst(temp.pollLast());
                }

                // temp空了，两个变量也要清理
                lastLast = null;
                last = null;

            } else {
                // System.out.println("5. 不是叶子节点");
                temp.offerLast(current);
                lastLast = last;
                last = current;
            }
        }

        return 1==temp.size() && "#".equals(temp.pollFirst());
    }
    */

    // 1. 根节点：0入，2出
    // 2. 其他节点：1入，2出
    // 3. null节点：1入，0出

    public boolean isValidSerialization(String preorder) {
        // 空节点，特殊
        if ("#".equals(preorder)) {
            return true;
        }

        int in = 0;
        int out = 0;

        String[] array = preorder.split(",");


        for (int i=0;i<array.length;i++) {

            if (0==i) {
                // 长度大于1的时候，根节点还要等于#，就是非法
                if ("#".equals(array[i])) {
                    return false;
                }

                // 1. 根节点：0入，2出
                out = 2;
                continue;
            }

            
            if ("#".equals(array[i])) {
                // 3. null节点：1入，0出
                in++;
            } else {
                // 2. 其他节点：1入，2出
                in++;
                out += 2;
            }

            // 不到最后，入度一定是小于出度的
            if (i<(array.length-1) && in>=out) {
                return false;
            }
        }

        return in==out;
    }

    public static void main(String[] args) {
        System.out.println(new L0331().isValidSerialization("9,3,4,#,#,1,#,#,2,#,6,#,#"));
        // System.out.println(new L0331().isValidSerialization("#,#,#"));
    }
}
