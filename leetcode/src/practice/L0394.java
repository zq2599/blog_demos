package practice;

import javax.xml.stream.events.StartDocument;

/**
 * @program: leetcode
 * @description:
 * @author: zq2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0394 {

    /**
     * 根据char数组的指定段落，构建字符串
     * @param array
     * @param start
     * @param end
     * @return
     */
    private String build(char[] array, int start, int end) {
        char[] temp = new char[end-start];
        System.arraycopy(array, start, temp, 0, end-start);
        return new String(temp);
    }

    /**
     * 找到结束括号的位置(考虑多层嵌套的情况)
     * @param array
     * @param start
     * @return
     */
    private int findEndBrace(char[] array, int start) {

        int startBraceNum = 0;

        for (int i=start;i<array.length;i++) {
            if ('['==array[i]) {
                startBraceNum++;
            }

            if (']'==array[i]) {
                if (1==startBraceNum) {
                    return i;
                } else {
                    startBraceNum--;
                }
            }
        }

        return 0;
    }


    private String singleBlock(char[] array, int start, int end) {

        StringBuilder sbud = new StringBuilder();
       
        int offset = start;

        int strStart;
        int val;
        int endBrace;

        while (offset<=end) {
        
            //  如果是字母
            if (array[offset]>='a' && array[offset]<='z') {
                
                // 记录字符串起始位置
                strStart = offset;

                while(offset<=end && 'a'<=array[offset] && array[offset]<='z') {
                    offset++;
                }

                sbud.append(build(array, strStart, offset));
                continue;
            }
    
            
            //  如果是数字
            if (array[offset]>='0' && array[offset]<='9') {
                
                val = array[offset++] - '0';
                
                while('0'<=array[offset] && array[offset]<='9') {
                    val = val*10 + array[offset++] - '0';
                }
                
                // 查找一对中括号结束的地方
                endBrace = findEndBrace(array, offset);

                // 递归
                String subContent = singleBlock(array, offset+1, endBrace-1);
        
                for (int i = 0; i < val; i++) {
                    sbud.append(subContent);
                }

                offset = endBrace+1;
            }
        }

        return sbud.toString();
    }

    public String decodeString(String s) {
        char[] array = s.toCharArray();
        return singleBlock(array, 0, array.length-1);
    }

    public static void main(String[] args) {
        // System.out.println(new L0394().decodeString("3[a]")); // aaabcbc
        // System.out.println("");

        // System.out.println(new L0394().decodeString("3[a]2[bc]")); // aaabcbc
        // System.out.println("");

        // System.out.println(new L0394().decodeString("3[a2[c]]")); // accaccacc
        // System.out.println("");

        // System.out.println(new L0394().decodeString("2[abc]3[cd]ef")); // abcabccdcdcdef
        // System.out.println("");

        // System.out.println(new L0394().decodeString("a")); // a
        // System.out.println("");

        // System.out.println(new L0394().decodeString("abc3[cd]xyz")); // abccdcdcdxyz
        // System.out.println("");

        System.out.println(new L0394().decodeString("3[z]2[2[y]pq4[2[jk]e1[f]]]ef")); // "zzzyypqjkjkefjkjkefjkjkefjkjkefyypqjkjkefjkjkefjkjkefjkjkefef"
        System.out.println("");

        // System.out.println(new L0394().decodeString("2[2[y]p]")); // "zzzyypqjkjkefjkjkefjkjkefjkjkefyypqjkjkefjkjkefjkjkefjkjkefef"
        // System.out.println("");

        // System.out.println(new L0394().getNumber("123abc".toCharArray()));
        // System.out.println(new L0394().getStr("aaa123".toCharArray()));
    }
}
