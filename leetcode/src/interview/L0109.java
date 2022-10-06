package interview;

public class L0109 {

    /*
    public boolean isFlipedString(String s1, String s2) {
        char[] array1 = s1.toCharArray();
        char[] array2 = s2.toCharArray();

        if (array1.length!=array2.length) {
            return false;
        }

        if (0==array1.length) {
            return true;            
        }

        if (1==array1.length) {
            return array1[0]==array2[0];
        }

        boolean isMatch;

        for (int step = 0; step < array2.length; step++) {
            
            isMatch = true;
            for (int j = 0; j < array2.length; j++) {
                if (array1[j]!=array2[(j+step)>(array1.length-1) ? (j+step-array1.length) : (j+step)]) {
                    // 一个比较失败就不继续了，
                    isMatch = false;
                    break;
                }
            }

            if (isMatch) {
                return true;
            }
        }


        return false;
    }
    */
    public boolean isFlipedString(String s1, String s2) {
        return s1.length()==s2.length() && (s1 + s1).indexOf(s2)>-1;
    }

    public static void main(String[] args) {
        System.out.println(new L0109().isFlipedString("abc", "bca"));
        System.out.println("");

        System.out.println(new L0109().isFlipedString("aa", "aba"));
        System.out.println("");

        System.out.println(new L0109().isFlipedString("a", "a"));
        System.out.println("");
    }
}
