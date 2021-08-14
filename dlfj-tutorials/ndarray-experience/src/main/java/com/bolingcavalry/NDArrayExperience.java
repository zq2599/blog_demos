package com.bolingcavalry;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Arrays;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 7/3/21 3:57 PM
 * @description 功能介绍
 */
public class NDArrayExperience {


    private static void disp(String type, INDArray indArray) {
        StringBuilder stringBuilder = new StringBuilder("*****************************************************\n");
        stringBuilder.append(type)
                     .append("\n维度 : ").append(indArray.rank())
                     .append("\n形状 : ").append(Arrays.toString(indArray.shape()))
                     .append("\n完整矩阵 : \n").append(indArray);

        System.out.println(stringBuilder);
    }

    public static void main(String[] args) {
        // 创建2行3列的全零矩阵
        INDArray indArray0 = Nd4j.zeros(2, 3);
        disp("全零矩阵", indArray0);

        // 创建2行3列的全一矩阵
        INDArray indArray1 = Nd4j.ones(2, 3);
        disp("全一矩阵", indArray1);

        // 创建2行3列的全是指定值的矩阵
        INDArray indArray2 = Nd4j.valueArrayOf(new int[] {2, 3}, 888);
        disp("全是指定值(888)的矩阵", indArray2);

        // 创建2行3列的随机矩阵
        INDArray indArray3 = Nd4j.rand(2, 3);
        disp("随机矩阵", indArray3);

        // 创建2行3列的随机高斯分布矩阵
        INDArray indArray4 = Nd4j.randn(2, 3);
        disp("随机高斯分布矩阵", indArray4);

        // 创建等差数列，
        // 从1到6、长度为10的等差数列
        INDArray indArray5 = Nd4j.linspace(1,6, 10);
        disp("等差数列", indArray5);

        // 根据数组创建2行3列的矩阵
        INDArray indArray6 = Nd4j.create(new float[] {1, 2, 3, 4, 5, 6}, new int[]  {2,3});
        disp("根据数组创建矩阵", indArray6);


        // 三维矩阵
        INDArray indArray7 = Nd4j.valueArrayOf(new int[] {2, 2, 3}, 888);
        disp("三维矩阵", indArray7);


        System.out.println("读取第一行第一列位置的值 : " + indArray6.getDouble(1,1));

        System.out.println("读取第一行 : " + indArray6.getRow(1));
        System.out.println("读取第二列 : " + indArray6.getColumn(2));
        System.out.println("读取第二、三列 : " + indArray6.getColumns(1,2));
        System.out.println("转为DataBuffer对象 : " + indArray6.data());


        indArray6.put(1,1, 123);
        indArray6.putScalar(0,0, 456);
        disp("a. 修改后", indArray6);

        // 准备一维数组
        INDArray row1 = Nd4j.create(new float[] {9,8,7});

        // 用一维数组替换矩阵的整行
        indArray6.putRow(1, row1);
        disp("b. 修改后", indArray6);

        // 重置
        indArray6 = Nd4j.create(new float[] {1, 2, 3, 4, 5, 6}, new int[]  {2,3});

        // 加法
        disp("加法", indArray6.add(1));

        // 减法
        disp("减法", indArray6.sub(1));

        // 乘法
        disp("乘法", indArray6.mul(2));

        // 除法
        disp("除法", indArray6.div(2));

        INDArray indArray8 = Nd4j.create(new float[] {1, 2, 3, 4, 5, 6}, new int[]  {2,3});
        disp("替换前", indArray8);
        indArray8.addi(1);
        disp("替换后", indArray8);


        // 展开
        disp("展开", Nd4j.toFlattened(indArray6));

        // 转换：2行3列转为3行2列
        disp("转换", indArray6.reshape(3,2));

        // 创建一个人3行3列的正方形矩阵
        INDArray indArray9 = Nd4j.create(new float[] {1, 2, 3, 4, 5, 6, 7, 8, 9}, new int[]  {3,3});
        disp("3*3矩阵", indArray9);

        // 提取正方形矩阵的对角线
        disp("3*3矩阵的对角线", Nd4j.diag(indArray9));

        // 创建3行3列的二维矩阵，对角线值为1.0
        INDArray indArray10 = Nd4j.eye(3);
        disp("3*3矩阵，且对角线都是1.0", indArray10);


        // 初始化一个2行3列的矩阵
        INDArray indArray11 = Nd4j.create(new float[] {1, 2, 3, 4, 5, 6}, new int[]  {2,3});
        // 参考indArray12的结构创建一个2行3列的矩阵，该矩阵的所有元素的值都等于10(入参)，
        // 然后，用该矩阵减去indArray11，结果作为rsub方法的返回值返回
        INDArray indArray12 = indArray11.rsub(10);
        disp("rsub方法", indArray12);



        INDArray indArray14 = Nd4j.create(new float[] {1, 1, 1, 1, 1, 1}, new int[]  {2,3});



        INDArray indArray13 = Nd4j.create(new float[] {1, 2, 3, 4, 5, 6}, new int[]  {2,3});
        INDArray indArray15 = Nd4j.create(new float[] {1, 2, 3, 4, 5, 6}, new int[]  {3,2});
        disp("2行3列", indArray13);
        disp("3行2列", indArray15);
        disp("2行3列矩阵与3行2列矩阵的叉乘", indArray13.mmul(indArray15));

        disp("矩阵相加", indArray13.add(indArray14));

        INDArray indArray16 = Nd4j.create(new float[] {1, 2, 3, 4, 5, 6}, new int[]  {2,3});
        // 总和
        double sum = indArray16.sum().getDouble();
        System.out.println("矩阵元素累加和 : " + sum);

        disp("转置前", indArray16);
        disp("转置操作", indArray16.transpose());
        disp("transpose操作后的原值(不变)", indArray16);

        disp("转置前", indArray16);
        disp("转置操作", indArray16.transposei());
        disp("transposei操作后的原值(已变)", indArray16);


        // 2行3列
        INDArray indArray17 = Nd4j.create(new float[] {1, 2, 3, 4, 5, 6}, new int[]  {2,3});
        // 2行1列
        INDArray indArray18 = Nd4j.create(new float[] {1, 2}, new int[]  {2,1});

        disp("源矩阵", indArray17);
        disp("拼接上的矩阵", indArray18);
        // 2行3列的矩阵，横向拼接一列后，变成了2行4列
        disp("横向拼接(每一行都增加一列)", Nd4j.hstack(indArray17, indArray18));

        // 2行3列
        INDArray indArray19 = Nd4j.create(new float[] {1, 2, 3, 4, 5, 6}, new int[]  {2,3});
        // 1行3列
        INDArray indArray20 = Nd4j.create(new float[] {1, 2, 3}, new int[]  {1,3});

        disp("源矩阵", indArray19);
        disp("拼接上的矩阵", indArray20);
        // 2行3列的矩阵，纵向拼接一行，变成了3行3列
        disp("纵向拼接(增加一行)", Nd4j.vstack(indArray19, indArray20));
    }
}
