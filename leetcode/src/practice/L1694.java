package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L1694 {

    public String reformatNumber(String number) {
        char[] rawArray = number.toCharArray();
        char[] tempArray = new char[rawArray.length + (rawArray.length/3)];

        int numberLen = 0;
        int offset = 0;

        for (int i=0;i<rawArray.length;i++) {
            // 空格和破折号都略过
            if(' '==rawArray[i] || '-'==rawArray[i]) {
                continue;
            }

            // 数字满了3个时，再增加数字就要增加一个破折号
            if (numberLen>0 && 0==numberLen%3) {
                tempArray[offset++] = '-';
                tempArray[offset++] = rawArray[i];
            } else {
                tempArray[offset++] = rawArray[i];
            }

            numberLen++;
        }

        if(numberLen<5) {
            return formatSmallStr(tempArray, 0, offset-1);
        } else {
            int mod = numberLen%3;

            switch (mod) {
                case 0:
                    return formatStr(tempArray, offset);

                case 1:
                    return formatStr(tempArray, offset-6)
                            + "-"
                            + formatSmallStr(tempArray, offset-5, offset-1);
                case 2:
                    // 减去破折号和最后两个数字
                    return formatStr(tempArray, offset-3)
                            + "-"
                            + formatSmallStr(tempArray, offset-2, offset-1);

            }
        }

        return "";
    }



    private String formatStr(char[] array, int len) {
        if (0==len) {
            return "";
        }

        char[] rltArray = new char[len];

        for (int i=0;i< rltArray.length;i++) {
            rltArray[i] = array[i];
        }

        return new String(rltArray);

    }

    /**
     * 长度小于等于4的字符数组的处理
     * @param array
     * @param start
     * @param end
     * @return
     */
    private String formatSmallStr(char[] array, int start, int end) {
        int len = end - start + 1;

        if (len<4) {
            char[] rlt = new char[len];

            for (int i=0;i< rlt.length;i++) {
                rlt[i] = array[start+i];
            }

            return new String(rlt);
        } else {
            String rltStr;
            // 因为有破折号，所以，只可能等于5
            char[] rlt = new char[2];

            rlt[0] = array[start];
            rlt[1] = array[start+1];

            rltStr = new String(rlt);

            rlt[0] = array[start+2];
            // 跳过破折号
            rlt[1] = array[start+4];

            rltStr += "-" + new String(rlt);
            return rltStr;
        }

    }




    public static void main(String[] args) {
//        System.out.println(new L1694().reformatNumber("1-23-45 6")); // 123-456
//        System.out.println(new L1694().reformatNumber("123 4-567")); // 123-45-67
//        System.out.println(new L1694().reformatNumber("123 4-5678")); // 123-456-78
//        System.out.println(new L1694().reformatNumber("12")); // 12
//        System.out.println(new L1694().reformatNumber("--17-5 229 35-39475 ")); // 175-229-353-94-75
        System.out.println(new L1694().reformatNumber("9052215-3-0--69-1849664-9585476671151 808 072253110132708288886781683799 715803761083")); // 175-229-353-94-75
    }
}