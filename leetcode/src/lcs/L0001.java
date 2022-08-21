package lcs;

import practice.TreeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0001 {

    /*
    public int reverseBits(int num) {
        // 特殊数字，32个0
        if(0==num) {
            return 1;
        }

        // 特殊数字，32个1
        if(-1==num) {
            return 32;
        }

        int[] lens = new int[32];
        int currentLensIndex = 0;

        for(int i=0;i<32;i++) {
            if (1==(num&1)) {
                lens[currentLensIndex]++;
            } else {
                currentLensIndex++;
            }

            // 右移一位
            num>>=1;
        }

        int max =0, joinLen;

        for (int i=1;i<=currentLensIndex;i++) {
            // 将自己和前面的连接起来
            joinLen = lens[i-1] + lens[i];

            if(max<joinLen) {
                max = joinLen;
            }
        }

        // 注意，题目的要求是改变一位，这就意味着连接的时候会多出一个1，所以要加1
        return max+1;
    }
    */

    public int reverseBits(int num) {
        // 特殊数字，32个0
        if(0==num) {
            return 1;
        }

        // 特殊数字，32个1
        if(-1==num) {
            return 32;
        }

        int currentLen = 0;
        int preLen = 0;
        int max = 0;

        for(int i=0;i<32;i++) {
            if (1==(num&1)) {
                currentLen++;

                // 循环的最后一位如果还是1，需要立即计算
                if (31==i) {
                    if (max<(preLen + currentLen)) {
                        max = preLen + currentLen;
                    }
                }
            } else {
                // 一旦遇到0，就表示连续的1结束了，需要立即计算
                if (max<(preLen + currentLen)) {
                    max = preLen + currentLen;
                }

                preLen = currentLen;
                currentLen = 0;
            }

            // 右移一位
            num>>=1;
        }

        // 注意，题目的要求是改变一位，这就意味着连接的时候会多出一个1，所以要加1
        return max+1;
    }

    public static void main(String[] args) {
        System.out.println(new L0001().reverseBits(1775));
        System.out.println(new L0001().reverseBits(7));
        System.out.println(new L0001().reverseBits(-1));
    }
}
