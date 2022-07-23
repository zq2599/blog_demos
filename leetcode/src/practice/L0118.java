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
public class L0118 {

    /*
    public List<List<Integer>> generate(int numRows) {
        List<List<Integer>> list = new ArrayList<>();

        // 第一行
        list.add(Arrays.asList(1));

        if (numRows<2) {
            return list;
        }

        // 第二行
        list.add(Arrays.asList(1,1));

        if (numRows<3) {
            return list;
        }

        // 这里用来记录前一行的内容
        int[] pre  = new int[] {1,1};

        int[] current;

        for (int i=2;i<numRows;i++) {
            // 代表当期行的数组
            current = new int[i+1];

            current[0] = 1;
            current[i] = 1;

            for (int j=1;j<i;j++) {
                current[j] = pre[j-1] + pre[j];
            }

            // 生成的list暂存在pre中，下一个循环用到
            pre = current;
            // 加入返回集合
            list.add(Arrays.stream(current).boxed().collect(Collectors.toList()));
        }

        return list;
    }
*/

    public List<List<Integer>> generate(int numRows) {
        List<List<Integer>> list = new ArrayList<>();

        // 第一行
        list.add(Arrays.asList(1));

        if (numRows<2) {
            return list;
        }

        // 第二行
        list.add(Arrays.asList(1,1));

        if (numRows<3) {
            return list;
        }

        for (int i=2;i<numRows;i++) {
            // 代表当期行的数组
            List<Integer> current = new ArrayList<>();
            current.add(1);

            for (int j=1;j<i;j++) {
                current.add(list.get(i-1).get(j-1) + list.get(i-1).get(j));
            }
            current.add(1);
            list.add(current);

        }

        return list;
    }



    public static void main(String[] args) {
        System.out.println(new L0118().generate(6));
    }
}
