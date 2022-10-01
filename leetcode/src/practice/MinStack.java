package practice;

import java.util.Deque;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class MinStack {
    Deque d;
    private int[] array = new int[4];
    private int pointer = -1;

    private int min = Integer.MAX_VALUE;

    public MinStack() {

    }

    public void push(int val) {
        array[++pointer] = val;
        min = Math.min(min, val);

        // 扩容
        if (pointer==(array.length-1)) {
            int[] temp = new int[array.length*2];
            System.arraycopy(array, 0, temp, 0, array.length);
            array = temp;
        }
    }

    public void pop() {
        pointer--;

        // 这里可以优化：如果弹出的不是最小值，那就没必要重算呀！
        if (array[pointer+1]==min) {
            min = Integer.MAX_VALUE;
            for (int i=0;i<=pointer;i++) {
                min = Math.min(min, array[i]);
            }
        }
    }

    public int top() {
        return array[pointer];
    }

    public int getMin() {
        return min;
    }

    public static void main(String[] args) {
        MinStack minStack = new MinStack();
        minStack.push(-2);
        minStack.push(0);
        minStack.push(-3);
        System.out.println(minStack.getMin());   //--> 返回 -3.
        minStack.pop();
        System.out.println(minStack.top());      //--> 返回 0.
        System.out.println(minStack.getMin());   //--> 返回 -2.

    }

}
