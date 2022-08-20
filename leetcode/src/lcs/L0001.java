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

    public int leastMinutes(int n) {

        int speed = 1;
        int sec = 0;
        while (speed<n) {
            speed <<= 1;
            sec++;
        }

        return sec +1;
    }


    public static void main(String[] args) {
        System.out.println(new L0001().leastMinutes(2));
        System.out.println(new L0001().leastMinutes(4));
    }
}
