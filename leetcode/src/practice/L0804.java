package practice;

import java.util.zip.Inflater;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0804 {



    private final static String[] MORSE = new String[]{".-","-...","-.-.","-..",".","..-.","--.","....","..",".---","-.-",".-..","--","-.","---",".--.","--.-",".-.","...","-","..-","...-",".--","-..-","-.--","--.."};


    private String morse(String word) {
        StringBuilder sbud = new StringBuilder();

        char[] array = word.toCharArray();

        for (int i=0;i<array.length;i++) {
            sbud.append(MORSE[array[i]-'a']);
        }

        return sbud.toString();
    }

    private boolean isSame(String a, String b) {

        if (a.length()!=b.length()) {
            return false;
        }

        char[] arrayA = a.toCharArray();
        char[] arrayB = b.toCharArray();

        for (int i=0;i<arrayA.length;i++) {
            if (arrayA[i]!=arrayB[i]) {
                return false;
            }
        }

        return true;
    }

    public int uniqueMorseRepresentations(String[] words) {
        if (words.length<2) {
            return words.length;
        }
        
        String[] uniqueMorses = new String[words.length];
        int uniqueNum = 0;

        String morse;
        boolean findSameFlag;

        for (int i=0;i<words.length;i++) {
            // 字符串转摩尔斯码
            morse = morse(words[i]);

            findSameFlag = false;

            for (int j = 0; j < uniqueNum; j++) {
                if (isSame(uniqueMorses[j], morse)) {
                    findSameFlag = true;
                    break;
                }
            }

            // 如果没找到重复的，就放入morses数组中
            if (!findSameFlag) {
              uniqueMorses[uniqueNum++] = morse;  
            }
        }

        return uniqueNum;
    }

    public static void main(String[] args) {
        System.out.println(new L0804().uniqueMorseRepresentations(new String[]{"gin", "zen", "gig", "msg"})); // 2
        System.out.println("");
        System.out.println(new L0804().uniqueMorseRepresentations(new String[]{"a"})); // 1
        System.out.println("");
    }
}
