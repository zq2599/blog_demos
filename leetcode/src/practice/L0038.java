package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0038 {

    /*
    public String countAndSay(int n) {

        char[] array;
        String val = "1";
        char prev;
        int len;
        StringBuilder sbud;

        for (int i = 1; i < n; i++) {
            array = val.toCharArray();

            // 接下来的处理的val长度都超过1，所以，对于val长度等于1的就特殊处理
            if (1==array.length) {
                val = "11";
                continue;
            }

            sbud = new StringBuilder();

            prev = array[0];
            len = 1;

            for (int j = 1; j < array.length; j++) {
                // 如果与之前的长度相等，就继续增加长度
                if (array[j]==prev) {
                    len++;
                } else {
                    // 如果于之前的不等，就触发描述
                    sbud.append(len).append(prev);
                    // 再重新设置当前值为prev
                    prev = array[j];
                    len = 1;
                }
            }

            // 最后还会触发一次
            sbud.append(len).append(prev);

            // 计算出新的val
            val = sbud.toString();
        }

        return val;
    }
    */

    public String countAndSay(int n) {

        char[] val = new char[4462];
        int valLen;

        // 初始值
        val[0] = '1';
        valLen = 1;

        char prev;
        int sigmentLen;

        char[] sbud = new char[4462];
        int len;

        char[] lenArray;

        for (int i = 1; i < n; i++) {

            // 接下来的处理的val长度都超过1，所以，对于val长度等于1的就特殊处理
            if (1==valLen) {
                val[1] = '1';
                valLen = 2;
                continue;
            }

            // len表示sbud中有效内容的长度
            len = 0;

            prev = val[0];
            sigmentLen = 1;

            for (int j = 1; j < valLen; j++) {
                // 如果与之前的长度相等，就继续增加长度
                if (val[j]==prev) {
                    sigmentLen++;
                } else {
                    // 如果于之前的不等，就触发描述
                    lenArray = String.valueOf(sigmentLen).toCharArray();

                    for (int k = 0; k < lenArray.length; k++) {
                        sbud[len++] = lenArray[k];
                    }

                    sbud[len++] = prev;
                    // 再重新设置当前值为prev
                    prev = val[j];
                    sigmentLen = 1;
                }
            }

            // 最后还会触发一次
            lenArray = String.valueOf(sigmentLen).toCharArray();

            for (int k = 0; k < lenArray.length; k++) {
                sbud[len++] = lenArray[k];
            }

            sbud[len++] = prev;

            // 计算出新的val
            valLen = len;

            for (int j = 0; j < len; j++) {
                val[j] = sbud[j];
            }

        }

        char[] rlt = new char[valLen];

        for (int i = 0; i < valLen; i++) {
            rlt[i] = val[i];
        }

        return new String(rlt);
    }

    public static void main(String[] args) {
        System.out.println(new L0038().countAndSay(1)); // 1
        System.out.println(new L0038().countAndSay(2)); // 11
        System.out.println(new L0038().countAndSay(3)); // 21
        System.out.println(new L0038().countAndSay(4)); // 1211
        System.out.println(new L0038().countAndSay(30).length()); // 1211
    }
}
