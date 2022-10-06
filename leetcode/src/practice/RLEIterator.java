package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class RLEIterator {

    private int currentPosition = 0;
    
    private int[] encoding;
    
    public RLEIterator(int[] encoding) {
        this.encoding = encoding;
    }
    
    public int next(int n) {
        for (int i=currentPosition;i<encoding.length;i+=2) {
            if (encoding[i]>n) {
                // 当前元素多于n个的时候，
                // 更新当前元素剩余的数量（耗去n个）
                encoding[i] -= n;
                // 返回耗去的最后一个
                return encoding[i+1];
            } else if (encoding[i]==n) {
                // 将当前元素刚好耗尽
                currentPosition += 2;    
                // 返回耗去的最后一个
                return encoding[i+1];
            } else {
                // 从n中减去当前n中剩余的数量，然后等for循环的下一个元素
                n -= encoding[i];
                // 将当前元素耗尽
                currentPosition += 2; 
            }
        }

        // 能走到这里显然是存货与已经耗尽
        return -1;
    }

    public static void main(String[] args) {
        int[] array = new int[] {3,8,0,9,2,5};

        // RLEIterator rleIterator = new RLEIterator(array);
        // System.out.println(rleIterator.next(2)); // 8
        // System.out.println(rleIterator.next(1)); // 8
        // System.out.println(rleIterator.next(1)); // 5
        // System.out.println(rleIterator.next(2)); // -1
        // System.out.println("");


//         ["RLEIterator","next","next","next","next","next","next"]
// [[[784,303,477,583,909,505]],[130],[333],[238],[87],[301],[276]]
// [null,303,303,303,583,583,505]

        System.out.println("---------");
        array = new int[] {784,303,477,583,909,505};
        RLEIterator rleIterator = new RLEIterator(array);
        System.out.println(rleIterator.next(130)); // 303
        System.out.println(rleIterator.next(333)); // 303
        System.out.println(rleIterator.next(238)); // 303
        System.out.println(rleIterator.next(87)); // 583
        System.out.println(rleIterator.next(301)); // 583
        System.out.println(rleIterator.next(276)); // 505
        System.out.println("");

    }

}
