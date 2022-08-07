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
public class L0636 {
    private int[] res;

    public int[] exclusiveTime(int n, List<String> logs) {

        res = new int[n];

        int[][] array = new int[logs.size()][3];

        // 将logs转成数组，后面都用数组array，减少字符串处理
        for (int i=0;i<array.length;i++) {
            String[] logInfo = logs.get(i).split(":");

            // 第0个字段是函数id
            array[i][0] = Integer.valueOf(logInfo[0]);

            // 第1个字段是标志：0是开始，1是结束
            if ("end".equals(logInfo[1])) {
                array[i][1] = 1;
            }
            // 第2个字段是时间
            array[i][2] = Integer.valueOf(logInfo[2]);
        }

        int[] startEndDfs = new int[] {0,0,-1};

        for (int i=0;i<array.length;i++) {
            if (i<=startEndDfs[2]) {
                continue;
            }

            startEndDfs = dfs(array, i);
        }

        return res;
    }

    private int[] dfs(int[][] array, int startIndex) {


        // 记录当前正在处理的函数的起始和截止时间，用于计算函数时长
        // 第0位：当前深度的起始时间
        // 第1位：当前深度的截止时间
        // 第2位：当前深度的结束位置
        int[] startEnd = new int[3];

        // 在调用dfs的时候，要记录dfs的计
        // 第0位：dfs的起始时间
        // 第1位：dfs的截止时间
        // 第2位：dfs的结束位置
        // 第2位：dfs的空白时间(两个函数之间不属于任何线程的时间，只能算作上一层的时间)
        // 注意startEndDfs[2]要等于-1，不然如果是0，下面的for循环中，i<=startEndDfs[2]就命中了
        int[] startEndDfs = new int[] {0,0,-1};
        int id = -1;

        int totalDfsTime = 0;

        for (int i=startIndex;i< array.length;i++) {

            // startEndDfs[2]是调用dfs时候返回的，表示dfs中指定过的位置，
            // 在当前循环中无需执行
            if (i<=startEndDfs[2]) {
                continue;
            }

            // 如果当前读到的是开始
            if(0==array[i][1]) {
                // 当前深度没有函数正在处理，就在当前深度处理
                if (-1==id) {
                    // 当前函数的起始时间
                    startEnd[0] = array[i][2];
                    id = array[i][0];
                } else {
                    startEndDfs = dfs(array, i);

                    // 调用时间累加
                    totalDfsTime += startEndDfs[1] - startEndDfs[0] + 1;
                }
            } else {
                // 读到了end，就在此累加到对应的函数时间上去
                startEnd[1] = array[i][2];

                // 自己的end减去start得到总时间，再减去调用其他函数的耗时
                res[id] += startEnd[1] - startEnd[0] + 1 - totalDfsTime;

                startEnd[2] = i;

                return startEnd;
            }
        }

        return startEnd;
    }



    public static void main(String[] args) {

        Tools.print(new L0636().exclusiveTime(2, new ArrayList<>(Arrays.asList("0:start:0","1:start:2","1:end:5","0:end:6"))));
        System.out.println("");


        Tools.print(new L0636().exclusiveTime(1, new ArrayList<>(Arrays.asList("0:start:0","0:start:2","0:end:5","0:start:6","0:end:6","0:end:7"))));
        System.out.println("");

        Tools.print(new L0636().exclusiveTime(2, new ArrayList<>(Arrays.asList("0:start:0","0:start:2","0:end:5","1:start:6","1:end:6","0:end:7"))));
        System.out.println("");

        Tools.print(new L0636().exclusiveTime(2, new ArrayList<>(Arrays.asList("0:start:0","0:start:2","0:end:5","1:start:7","1:end:7","0:end:8"))));
        System.out.println("");

        Tools.print(new L0636().exclusiveTime(1, new ArrayList<>(Arrays.asList( "0:start:0","0:end:0" ))));
        System.out.println("");

        Tools.print(new L0636().exclusiveTime(3, new ArrayList<>(Arrays.asList( "0:start:0","0:end:0","1:start:1","1:end:1","2:start:2","2:end:2","2:start:3","2:end:3" ))));
    }
}
