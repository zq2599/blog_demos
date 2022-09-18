package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0344 {


    public void reverseString(char[] s) {
        char temp;

        for (int i=0;i<s.length/2;i++) {
            temp = s[i];
            s[i] = s[s.length-1-i];
            s[s.length-1-i] = temp;
        }
    }

    public static void main(String[] args) {
        char[] s = new char[]{'h','e','l','l','o'};
        new L0344().reverseString(s);
        Tools.print(s);
    }
}
