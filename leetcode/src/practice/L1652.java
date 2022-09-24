package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L1652 {

    /**
     * 根据当前位置、总长、k，得到用于相加的元素的全部索引
     * @param currentIndex
     * @param arrayLen
     * @param k
     * @return 用于累加的元素的位置，注意，是位置
     */
    /*
    private int[] getIndexForAdd(int currentIndex, int arrayLen, int k) {
        int[] rlt = new int[k>0 ? k : -k];

        if (k>0) {
            // 根据题目，k大于零的时候，是在i位置往后找
            for (int i=0;i<k;i++) {
                // 如果没有超出长度，就用i后面的
                if ((currentIndex+i+1)<=(arrayLen-1)) {
                    rlt[i] = currentIndex+i+1;
                } else {
                    // 如果超出了长度，就从头开始
                    rlt[i] = currentIndex+i+1-arrayLen;
                }
            }
        } else {
            // 根据题目，k小于零的时候，是在i位置往前找
            for (int i=0;i<-k;i++) {
                // 如果没有到达数组头部，就继续往头部走
                if ((currentIndex-i-1)>=0) {
                    rlt[i] = currentIndex-i-1;
                } else {
                    // 如果到达头部，就从尾部往前走
                    rlt[i] = arrayLen + (currentIndex-i-1);
                }
            }
        }

        return rlt;
    }

    public int[] decrypt(int[] code, int k) {
        // 0最特殊
        if (0==k) {
            return new int[code.length];
        }

        int[] indexArray;
        int rlt[] = new int[code.length];
        int len = k>0 ? k : -k;

        for(int i=0;i<code.length;i++) {
            // 计算出当前要累加那几个元素之和
            indexArray = getIndexForAdd(i, code.length, k);

            // 逐一累加
            for(int j=0;j<len;j++) {
                rlt[i] += code[indexArray[j]];
            }
        }

        return rlt;
    }
    */

    public int[] decrypt(int[] code, int k) {
        // 0最特殊
        if (0==k) {
            return new int[code.length];
        }

        int rlt[] = new int[code.length];

        if (k>0) {
            for(int i=0;i<code.length;i++) {
                for (int j=0;j<k;j++) {
                    // 如果没有超出长度，就用i后面的
                    if ((i+j+1)<=(code.length-1)) {
                        rlt[i] += code[i+j+1];
                    } else {
                        // 如果超出了长度，就从头开始
                        rlt[i] += code[i+j+1-code.length];
                    }
                }
            }
        } else {
            for(int i=0;i<code.length;i++) {
                // 根据题目，k小于零的时候，是在i位置往前找
                for (int j=0;j<-k;j++) {
                    // 如果没有到达数组头部，就继续往头部走
                    if ((i-j-1)>=0) {
                        rlt[i] += code[i-j-1];
                    } else {
                        // 如果到达头部，就从尾部往前走
                        rlt[i] += code[code.length + (i-j-1)];
                    }
                }
            }
        }

        return rlt;
    }


    public static void main(String[] args) {
        Tools.print(new L1652().decrypt(new int[]{5,7,1,4}, 3)); // 12,10,16,13
        Tools.print(new L1652().decrypt(new int[]{1,2,3,4}, 0)); // 0,0,0,0
        Tools.print(new L1652().decrypt(new int[]{2,4,9,3}, -2)); // 12,5,6,13
    }
}
