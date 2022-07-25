package offer2;

import java.util.*;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0115 {

    // 一定要审题，下面几个是前提：
    // 首先，sequences[i]是nums的子序列，所以nums={1,2}，sequences[i]={3,4}这种情况不存在
    // 其次，nums 是 [1, n] 范围内所有整数的排列，那就不存在[1,2,2]这样的东西，作为其子序列的sequences[i]也就不存在[1,2,2]

    // 1. 建拓扑：所谓建图，就是每个元素和其相邻元素的关系
    // 2. 拓扑排序：排序过程中，如果入度为0的元素超过一个，就意味的有多个拓扑序列，也就是说int[][] sequences的最短超序列不唯一

    public boolean sequenceReconstruction(int[] nums, int[][] sequences) {
        // 1-n的每个数字的入度，
        // 为了计算方便，将inTimes的长度增加一个，这样inTimes[3]就代表3的入度，而无需从0开始了
        int[] inTimes = new int[nums.length+1];

        // 用Map做拓扑图的邻接表
        Map<Integer, List<Integer>> dagMap = new HashMap<>();

        // 过滤集合
        // 如果sequences={{1,2}, {1,3}}，那么nums就只能有{1,2,3}，因为其他的都和如果sequences无关，
        // 一旦出现其他元素，那么nums就不是sequences的最短超序列了，
        // 所以把sequences的所有元素都在existsFlags中的对应位置设置为true，例如sequences[i]={1,3}，那么existsFlags[1]=true,existsFlags[3]=true
        // 所以只要existsFlags每个元素都是true，就证明nums中的元素和sequences的元素完全相等，不存在多余元素

        boolean[] existsFlags = new boolean[nums.length+1];

        // 遍历sequences中的所有数组，构建拓扑图
        for (int i=0;i<sequences.length;i++) {
            // 当前数组
            int[] array = sequences[i];

            existsFlags[array[0]] = true;

            for (int j=1;j<array.length;j++) {
                // array[j]这个值在existsFlags的标志设置为true
                existsFlags[array[j]] = true;
                // from和to代表拓扑图中带方向连接的两个点
                int from = array[j-1];
                int to = array[j];

                // 如果sequences={{1,2}, {1,3}}，那么dagMap中就有一个键值对，其键为1，值为所有和1相连的数字，这里是2和3，把2和3放入list集合中
                dagMap.computeIfAbsent(from, key -> new ArrayList<>()).add(to);

                // 如果from=1，to=2，那么作为被连接元素的2，其入度应该加一
                inTimes[to]++;
            }
        }

        // 例如nums={1,2,3}，sequences={{1,2}}，
        // 那么existsFlags应该是长度为4的数组，值应该是{false,true,true,false}，
        // existsFlags[0]没有用到，existsFlags[3]等于false，因为sequences中没有3，所以不会有设置existsFlags[3]=true的机会，
        // 因此从第一位开始检查，只要有false，就表示nums有的值在sequences中不存在，那么nums就不是最短超序列了
        for (int i=1;i<= nums.length;i++) {
            if(!existsFlags[i]) {
                return false;
            }
        }

        // 前面的existsFlags解决了nums是不是最短超序列的问题，现在要解决的是nums是不是唯一的最短超序列，
        // 做法就是拓扑排序，只要发现入度等于0的元素超过一个，就证明拓扑序列有多个，那就不满足条件了

        // 用deque来存在所有入度为0的元素
        Deque<Integer> deque = new ArrayDeque<>();

        // 注意要从1开始
        for(int i=1;i<inTimes.length;i++) {
            if (0==inTimes[i]) {
                deque.push(i);
            }
        }

        // 接下来，开始常规的拓扑排序操作
        while (!deque.isEmpty()) {
            // 入度为0的元素如果超过1，就证明拓扑序列有多个，那就不满足条件了
            if(deque.size()>1) {
                return false;
            }

            // 按照常规拓扑排序的操作流程，先移除入度为0的元素，再解除的元素的相邻关系，解除关系的时候，相邻元素的入度也要更新
            // 1. 先移除入度为0的元素
            int value = deque.pop();
            // 2. 再解除的元素的相邻关系（这里其实无所谓解不解除，放在map中也没啥影响，因为不会再用这个key了）
            List<Integer> neighbours = dagMap.get(value);
            // 3. 解除关系的时候，相邻元素的入度也要更新
            if (null!=neighbours) {
                for(Integer neighbour : neighbours) {
                    inTimes[neighbour]--;
                    // 一旦出现入度为0的元素，就放入deque中，作为下一轮拓扑排序的元素，这是拓扑排序的标准操作
                    if (0==inTimes[neighbour]) {
                        deque.push(neighbour);
                    }
                }
            }
        }

        return true;
    }

    public static void main(String[] args) {
        int[] nums = {1, 2, 3};
        int[][] sequences = {{1, 2}, {1, 3}, {2, 3}};
        System.out.println(new L0115().sequenceReconstruction(nums, sequences));
    }
}
