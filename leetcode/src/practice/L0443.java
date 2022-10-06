package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0443 {

    private int addToCharArray(char[] array, int start, int val) {
        int len = 0;

        int calc = val;
        while (calc > 0) {
            len++;
            calc /= 10;
        }

        calc = val;

        for (int i = 0; i < len; i++) {
            array[start + len - i - 1] = (char) ('0' + (calc % 10));
            calc /= 10;
        }

        return len;
    }

    public int compress(char[] chars) {

        char[] array = new char[chars.length];

        // 返回的，总长度
        int totalLen = 0;

        // 在循环中用到的，记录当前字符的长度，
        // 注意，因为for循环是从1开始的，所以currentLetterLen记录的是chars[0]字符的长度
        int currentLetterLen = 1;

        // 由于每次都和前一个比较，所以循环要从1开始
        for (int i = 1; i < chars.length; i++) {

            // 如果当前字符和前一个字符相等，currentLetterLen就加一
            if (chars[i] == chars[i - 1]) {
                currentLetterLen++;
            } else {
                // 如果当前字符和前一个字符不等，就要统计前一个字符及其长度所占用的字符串长度了
                if (currentLetterLen > 1) {
                    // 先加字母
                    array[totalLen++] = chars[i-1];
                    // addToCharArray会把currentLetterLen转为字符串，然后添加到array数组中，返回值是currentLetterLen转为字符串后的长度
                    totalLen += addToCharArray(array, totalLen, currentLetterLen);
                } else {
                    // 按照规则，长度为1时，只加上字母
                    array[totalLen++] = chars[i-1];
                }

                // 字母变了，立即将长度改成1（当前字符）
                currentLetterLen = 1;
            }
        }

        // 别忘了最后一组
        // 如果当前字符和前一个字符不等，就要统计前一个字符及其长度所占用的字符串长度了
        if (currentLetterLen > 1) {
            // 先加字母
            array[totalLen++] = chars[chars.length - 1];
            // addToCharArray会把currentLetterLen转为字符串，然后添加到array数组中，返回值是currentLetterLen转为字符串后的长度
            totalLen += addToCharArray(array, totalLen, currentLetterLen);
        } else {
            // 按照规则，长度为1时，只加上字母
            array[totalLen++] = chars[chars.length - 1];
        }

        for (int i = 0; i < totalLen; i++) {
            chars[i] = array[i];
        }

        return totalLen;
    }

    public static void main(String[] args) {
        System.out.println("***");
        System.out.println(new L0443().compress(new char[] { 'a', 'a', 'b', 'b', 'c', 'c', 'c' })); // 6
        System.out.println("***");
        System.out.println(new L0443().compress(new char[] { 'a' })); // 1
        System.out.println("***");
        System.out.println(
                new L0443().compress(new char[] { 'a', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b' })); // 4

        System.out.println("");
    }

}
