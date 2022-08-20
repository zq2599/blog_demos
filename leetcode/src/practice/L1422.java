package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L1422 {




    public int maxScore(String s) {

        char[] array = s.toCharArray();

        int prevLeftScore = 0;
        int prevRightScore = 0;


        for (int i=0;i<array.length;i++) {
            if('1'==array[i]) {
                prevRightScore++;
            }
        }

        // 暂时以
        int maxScore = 0;
        int currentScore = 0;

        for(int i=0;i< array.length-1;i++) {
            if ('0'==array[i]) {
                prevLeftScore++;
            } else {
                prevRightScore--;
            }

            currentScore = prevLeftScore + prevRightScore;
            maxScore = Math.max(maxScore, currentScore);
        }

        return maxScore;
    }

    public static void main(String[] args) {
        System.out.println(new L1422().maxScore("011101"));
        System.out.println(new L1422().maxScore("00111"));
        System.out.println(new L1422().maxScore("1111"));
        System.out.println(new L1422().maxScore("00"));
    }
}
