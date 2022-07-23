package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0069 {

    public int mySqrt(int x) {
        if (x<2) {
            return x;
        }

        return (int)sqrt(x, x);
    }

    /**
     * 必须要用double，如果用float，在计算2147395599的时候无法通过，估计是精度不够
     * @param x
     * @param val
     * @return
     */
    private double sqrt(double x, double val) {
        // 逼近一次
        double nearVal = 0.5 * (x + val/x);

        // 逼近后和逼近前如果相等（double的精度），那就没有必要再算了
        if (nearVal==x) {
            return nearVal;
        }

        return sqrt(nearVal, val);
    }



    public static void main(String[] args) {
        System.out.println(new L0069().mySqrt(2147395599));
    }
}
