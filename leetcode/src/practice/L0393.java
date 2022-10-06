package practice;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0393 {

    public boolean validUtf8(int[] data) {
        // index有个特点：一定指向一个utf8数字的起始位置
        int index = 0;
        
        while(index<data.length) {
            // 检查是不是1字节的编码(最高位等于0的话，与128应该等于0)
            if (0==(data[index] & 128)) {
                index++;
                continue;
            }

            // 如果还没到底，就检查是不是2字节的编码
            if (data.length>1 && index<(data.length-1)) {
                if (data[index]>>5==6 && data[index+1]>>6==2) {
                    index += 2;
                    continue;
                } 
            }

            // 如果还没到底，就检查是不是3字节的编码
            if (data.length>2 && index<(data.length-2)) {
                if (data[index]>>4==14 
                && data[index+1]>>6==2
                && data[index+2]>>6==2) {
                    index += 3;
                    continue;
                } 
            }

            // 如果还没到底，就检查是不是4字节的编码
            if (data.length>3 && index<(data.length-3)) {
                if (data[index]>>3==30 
                && data[index+1]>>6==2
                && data[index+2]>>6==2
                && data[index+3]>>6==2) {
                    index += 4;
                    continue;
                } 
            }

            // 如果一个都没有命中，那就不是utf8编码了
            return false;
        }

        return true;
    }



    public static void main(String[] args) {
        // System.out.println(new L0393().validUtf8(new int[]{197,130,1}));
        System.out.println(new L0393().validUtf8(new int[]{235,140,4}));
    }
}
