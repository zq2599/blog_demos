package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0014 {

    public String longestCommonPrefix(String[] strs) {
        if (strs.length<1) {
            return "";
        }

        int length = strs[0].length();

        // 确定检查的最大长度，即最短字符串的长度
        for (int i=1;i<strs.length;i++) {
            if(strs[i].length()<length) {
                length = strs[i].length();
            }
        }

        StringBuilder sbud = new StringBuilder();

        char currentChar;

        // 逐个字符检查
        for (int charIndex=0;charIndex<length;charIndex++) {

            currentChar = strs[0].charAt(charIndex);

            for(int strIndex=1;strIndex<strs.length;strIndex++) {
                if(currentChar!=strs[strIndex].charAt(charIndex)){
                    return sbud.toString();
                }
            }

            sbud.append(currentChar);
        }

        return sbud.toString();
    }


    public static void main( String[] args ) {
        System.out.println(new L0014().longestCommonPrefix(new String[] {
                "flower",
                "flow",
                "flight"}));
    }
}
