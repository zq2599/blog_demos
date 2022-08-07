package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0622 {

    class MyCircularQueue {
        // 头指针
        private int head = 0;

        // 尾指针
        private int tail = -1;

        private int[] array;

        public MyCircularQueue(int k) {
            array = new int[k];
        }

        private int len() {
            if (-1==tail) {
                return 0;
            }

            if (head<=tail) {
                return tail - head + 1;
            }

            return (array.length-head) + (tail+1);
        }

        public boolean enQueue(int value) {
            int len = len();

            // 满了
            if (len==array.length) {
                return false;
            }

            // 为即将加入的数据准备位置，也就是tail的值
            // 如果为空，就应该放入head位置
            if (0==len) {
                tail = head;
            } else {
                // 如果已经存到了数组尾部，就注意接下来应该存入数组开始位置，因为是个环
                if (tail==(array.length-1)) {
                    tail =0;
                } else {
                    // 这是正常情况，将数据往数组后面cun
                    tail++;
                }
            }

            array[tail] = value;
            return true;
        }

        public boolean deQueue() {
            int len = len();
            if (len<1) {
                return false;
            }

            // head后移，注意边界
            if(head==(array.length-1)) {
                head = 0;
            } else {
                head++;
            }

            // 如果只有一条记录，现在应该没有数据了
            if (1==len) {
                tail = -1;
            }

            return true;
        }

        public int Front() {
            return -1==tail ? -1 : array[head];
        }

        public int Rear() {
            return -1==tail ? -1 : array[tail];
        }

        public boolean isEmpty() {
            return -1==tail;
        }

        public boolean isFull() {
            return len()==array.length;
        }
    }

    public void test() {
        /*
        MyCircularQueue circularQueue = new L0622.MyCircularQueue(8); // 设置长度为 3
        System.out.println(circularQueue.enQueue(3)); // true
        System.out.println(circularQueue.enQueue(9)); // true
        System.out.println(circularQueue.enQueue(5)); // true
        System.out.println(circularQueue.enQueue(0)); // true
        System.out.println(circularQueue.deQueue()); // true
        System.out.println(circularQueue.deQueue()); // true
        System.out.println(circularQueue.isEmpty()); // false
        System.out.println(circularQueue.isEmpty()); // false
        System.out.println(circularQueue.Rear()); // 0
        System.out.println(circularQueue.Rear()); // 0
        System.out.println(circularQueue.deQueue()); // true
         */

        /*
        MyCircularQueue circularQueue = new L0622.MyCircularQueue(3);
        System.out.println(circularQueue.enQueue(1)); // true
        System.out.println(circularQueue.enQueue(2)); // true
        System.out.println(circularQueue.enQueue(3)); // true
         */

        /*
        ["MyCircularQueue","enQueue","Rear","Front","deQueue","Front","deQueue","Front","enQueue","enQueue","enQueue","enQueue"]
[[3],[2],[],[],[],[],[],[],[4],[2],[2],[3]]
[null,true,2,2,true,-1,false,-1,true,true,true,false]
        */

        MyCircularQueue circularQueue = new L0622.MyCircularQueue(3);
        System.out.println(circularQueue.enQueue(2)); // true
        System.out.println(circularQueue.Rear()); // 2
        System.out.println(circularQueue.Front()); // 2
        System.out.println(circularQueue.deQueue());  // true
        System.out.println(circularQueue.Front()); // -1
        System.out.println(circularQueue.deQueue()); // false
        System.out.println(circularQueue.Front()); // -1
        System.out.println(circularQueue.enQueue(4)); // true
        System.out.println(circularQueue.enQueue(2)); // true
        System.out.println(circularQueue.enQueue(2)); // true
        System.out.println(circularQueue.enQueue(3)); // false

    }



    public static void main(String[] args) {
        new L0622().test();
    }
}
