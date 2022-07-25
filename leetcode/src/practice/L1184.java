package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L1184 {

    public int distanceBetweenBusStops(int[] distance, int start, int destination) {
        // 注意审题，distance长度可能等于1
        if(1==distance.length) {
            return distance[0];
        }

        // 注意审题，并没说start必须小于destination，所以要检查，
        // 如果发现start与destination相等，就直接返回0
        // 如果发现start比destination大，就要颠倒过来
        if (start==destination) {
            return 0;
        } else if (start>destination) {
            int temp = start;
            start = destination;
            destination = temp;
        }

        int forward = 0, reverse = 0;

        for (int i=0;i<distance.length;i++) {

            if (i>=start && i<destination) {
                forward += distance[i];
            } else {
                reverse += distance[i];
            }
        }

        return forward<reverse ? forward : reverse;
    }



    public static void main(String[] args) {
        System.out.println(new L1184().distanceBetweenBusStops(new int[] {1,2,3,4},0, 1));
        System.out.println(new L1184().distanceBetweenBusStops(new int[] {7,10,1,12,11,14,5,0},7, 2));
    }
}
