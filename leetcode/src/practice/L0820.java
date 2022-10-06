package practice;

import java.util.Arrays;
import java.util.Comparator;

import javax.xml.stream.events.StartDocument;

/**
 * @program: leetcode
 * @description:
 * @author: zq2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0820 {

    // 准备一个大型的char数组，假设可以存下所有内容，按照最大的估算
    char[] array;

    // 记录array用掉的长度
    int arrayUsedLen;

    // 保存压缩后每个单词的结束位置（注意，是结束位置！）
    int[] endPositions;
    
    // 记录当前已经有压缩有有多少单词了，即startPositions的真实用量
    int endPositionsNum;

    int[] indices;

    int indicesNum;


    private int match(char[] currentWord) {

        for (int i = 0; i < endPositionsNum; i++) {
            // System.out.println("1. 开始检查[" + endPositions[i] + "]");
            // 注意结束条件，本来是到前一个单词末尾的"#"就要结束，
            // 但是由于单词中没有"#"，所以比较的时候，遇到"#"肯定不等，就会结束了
            for (int j=0;j<=(currentWord.length-1) && j<=endPositions[i];j++) {
                // 遇到一个字母不等，就不再比较，改为比较下一个
                if (currentWord[currentWord.length-j-1]!=array[endPositions[i]-j]) {
                    break;
                }

                // 能走到这里，表示已经完全匹配了
                if (j==(currentWord.length-1)) {
                    // System.out.println("2. 匹配到了位置[" + (endPositions[i]-j) + "]");
                    return endPositions[i]-j;
                }
            } 
        }

        return -1;
    }

    private void compressOne(char[] word) {

        for (int i=0;i<word.length;i++) {
            array[arrayUsedLen+i] = word[i];
        }

        array[arrayUsedLen+word.length] = '#';

        // 新增单词的结束位置
        endPositions[endPositionsNum++] = arrayUsedLen + word.length - 1;

        // 更新用掉的array长度
        arrayUsedLen += word.length + 1;
    }


    public int minimumLengthEncoding(String[] words) {
        Arrays.sort(words, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.length() - o1.length();
            }
        });


        // 准备一个大型的char数组，假设可以存下所有内容，按照最大的估算
        array = new char[words.length*7 + words.length];

        // 记录array用掉的长度
        arrayUsedLen = 0;

        // 保存压缩后每个单词的起始位置
        endPositions = new int[words.length];
        
        // 记录当前已经有压缩有有多少单词了，即startPositions的真实用量
        endPositionsNum = 0;

        indices = new int[words.length];

        indicesNum = 0;


        int matchPostion;
        char[] currentWord;

        for (int i=0;i<words.length;i++) {
            currentWord = words[i].toCharArray();

            matchPostion = match(currentWord);

            // 如果在现有压缩内容中能匹配到，就直接更新
            if (matchPostion>-1) {
                indices[indicesNum++] = matchPostion;
            } else {
                // 如果没有找到，就放入压缩内容中
                compressOne(currentWord);
            }
        }

        return arrayUsedLen;
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

        // System.out.println(new L0820().minimumLengthEncoding(new String[] {"time", "me", "bell"})); // 10
        // System.out.println("");

        // System.out.println(new L0820().minimumLengthEncoding(new String[] {"t"})); // 10
        // System.out.println("");

        System.out.println(new L0820().minimumLengthEncoding(new String[] {"me","time"})); // 10
        System.out.println("");

        // System.out.println(new L0394().decodeString("2[2[y]p]")); // "zzzyypqjkjkefjkjkefjkjkefjkjkefyypqjkjkefjkjkefjkjkefjkjkefef"
        // System.out.println("");

        // System.out.println(new L0394().getNumber("123abc".toCharArray()));
        // System.out.println(new L0394().getStr("aaa123".toCharArray()));
    }
}
