package com.bolingcavalry;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 7/3/21 3:57 PM
 * @description 功能介绍
 */
public class NDArrayExperience {
    public static void main(String[] args) {

        INDArray indArray = Nd4j.create(new int[]  {3,2}, new float[] {1, 2, 3, 4, 5, 6});

        System.out.println(indArray);


    }
}
