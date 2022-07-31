package practice;

import java.util.*;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0952 {

    private static int max = 100000;

    private static int[] isPrime = null;
    private static int[] primes = null;

    // 素数的数量
    private static int primeNum = 0;

    // 并查集的数组， fathers[3]=1的意思是：数字3的父节点是1
    private static int[] fathers;

    // 并查集中，每个数字与其子节点的元素数量总和，rootSetSize[5]=10的意思是：数字5与其所有子节点加在一起，一共有10个元素
    private static int[] rootSetSize;

    // map的key是质因数，value是以此key作为质因数的数字
    // 例如题目的数组是[4,6,15,35]，对应的map就有四个key：2,3,5,7
    // key等于2时，value是[4,6]，因为4和6的质因数都有2
    // key等于3时，value是[6,15]，因为6和16的质因数都有3
    // key等于5时，value是[15,35]，因为15和35的质因数都有5
    // key等于7时，value是[35]，因为35的质因数有7
    private static Map<Integer, List<Integer>> map = null;

    static {
        isPrime = new int[max+1];
        primes = new int[max+1];

        // 欧拉筛
        for(int i=2;i<=max;i++) {
            if(isPrime[i]==0) {
                // i是素数，就放入primes数组中
                // 更新primes中素数的数量
                primes[primeNum++] = i;
            }

            for(int j=0;i*primes[j]<=max;j++) {
                // primes[j]*i的结果是个乘积，这样的数字显然不是素数，所以在isPrimes数组中标注为1
                isPrime[primes[j]*i] = 1;

                // 如果i是primes中某个素数的倍数，就没有必要再计算了，退出算下一个，
                // 例如i=8的时候，其实在之前i=4时就已经计算出8不是素数了
                if(i%primes[j]==0) {
                    break;
                }
            }
        }

        // 并查集的数组， fathers[3]=1的意思是：数字3的父节点是1
        fathers = new int[20001];

        // 并查集中，每个数字与其子节点的元素数量总和，rootSetSize[5]=10的意思是：数字5与其所有子节点加在一起，一共有10个元素
        rootSetSize = new int[20001];

        map = new HashMap<>(primeNum);
    }



    // 用来保存并查集中，最大树的元素数量
    int maxRootSetSize = 1;

    /**
     * 带压缩的并查集查找(即寻找指定数字的根节点)
     * @param i
     */
    private int find(int i) {
        // 如果执向的是自己，那就是根节点了
        if(fathers[i]==i) {
            return i;
        }

        // 用递归的方式寻找，并且将整个路径上所有长辈节点的父节点都改成根节点，
        // 例如1的父节点是2，2的父节点是3，3的父节点是4，4就是根节点，在这次查找后，1的父节点变成了4，2的父节点也变成了4，3的父节点还是4
        fathers[i] = find(fathers[i]);
        return fathers[i];

        /*
        while (fathers[i]!=i) {
            fathers[i] = fathers[fathers[i]];
            i = fathers[i];
        }

        return fathers[i];
        */
    }

    /**
     * 并查集合并，合并后，child会成为parent的子节点
     * @param parent
     * @param child
     */
    private void union(int parent, int child) {
        int parentRoot = find(parent);
        int childRoot = find(child);

        // 如果有共同根节点，就提前返回
        if (parentRoot==childRoot) {
            return;
        }

        // child元素根节点是childRoot，现在将childRoot的父节点从它自己改成了parentRoot，
        // 这就相当于child所在的整棵树都拿给parent的根节点做子树了
        fathers[childRoot] = fathers[parentRoot];

        // 合并后，这个树变大了，新增元素的数量等于被合并的字数元素数量
        rootSetSize[parentRoot] += rootSetSize[childRoot];

        // 更新最大数量
        maxRootSetSize = Math.max(maxRootSetSize, rootSetSize[parentRoot]);
    }

    public int largestComponentSize(int[] nums) {
        map.clear();

        // 对数组中的每个数，算出所有质因数，构建map
        for (int i=0;i<nums.length;i++) {
            int cur = nums[i];

            // cur的质因数一定是primes中的一个
            for (int j=0;j<primeNum && primes[j]*primes[j]<=cur;j++) {
                if (cur%primes[j]==0) {
//                    addToMap(primes[j], i);
                    map.computeIfAbsent(primes[j], key -> new ArrayList<>()).add(i);

                    // 要从cur中将primes[j]有关的倍数全部剔除，才能检查下一个素数
                    while (cur%primes[j]==0) {
                        cur /= primes[j];
                    }
                }
            }

            // 能走到这里依然不等于1，是因为for循环中的primes[j]*primes[j]<<=cur导致了部分素数没有检查到，
            // 例如6，执行了for循环第一轮后，被2除过，cur等于3，此时j=1,那么primes[j]=3，因此 3*3无法小于cur的3，于是退出for循环，
            // 此时cur等于3，应该是个素数，所以nums[i]就能被此时的cur整除，那么此时的cur就是nums[i]的质因数，也应该放入map
            if (cur>1) {
//                addToMap(cur, i);
                map.computeIfAbsent(cur, key -> new ArrayList<>()).add(i);
            }
        }

        // 至此，map已经准备好了，接下来是并查集的事情，先要初始化数组
        for(int i=0;i< fathers.length;i++) {
            // 这就表示：数字i的父节点是自己
            fathers[i] = i;
            // 这就表示：数字i加上其下所有子节点的数量等于1（因为每个节点父节点都是自己，所以每个节点都没有子节点）
            rootSetSize[i] = 1;
        }

        // 遍历map
        for (int key : map.keySet()) {
            // 每个key都是一个质因数
            // 每个value都是这个质因数对应的数字
            List<Integer> list = map.get(key);

            // 超过1个元素才有必要合并
            if (list.size()>1) {
                // 取第0个元素作为父节点
                int parent = list.get(0);

                // 将其他节点全部作为地0个元素的子节点
                for(int i=1;i<list.size();i++) {
                    union(parent, list.get(i));
                }
            }
        }

        return maxRootSetSize;
    }



    public static void main(String[] args) {
        System.out.println(new L0952().largestComponentSize(new int[] {4,6,15,35}));
        System.out.println(new L0952().largestComponentSize(new int[] {20,50,9,63}));
        System.out.println(new L0952().largestComponentSize(new int[] {2,3,6,7,4,12,21,39}));
    }
}
