package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0278 {

    private static final int BAD = 4;


    private boolean isBadVersion(int n) {
       return n>=BAD;
    }


    public int firstBadVersion(int n) {
        int start = 1;
        int middle;

        while (start<n) {
            middle = start + (n-start)/2;

            if(isBadVersion(middle)) {
                n = middle;
            } else {
                start = middle+1;
            }
        }

        return start;
    }

    public static void main(String[] args) {
        System.out.println(new L0278().firstBadVersion(5));
    }
}