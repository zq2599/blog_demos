package interview;

public class L0102 {

    public boolean CheckPermutation(String s1, String s2) {
        int[] chars = new int[26];

        char[] array1 = s1.toCharArray();
        char[] array2 = s2.toCharArray();

        for (int i=0;i<s1.length();i++) {
            chars[array1[i]-'a']++;
        }

        int index;

        for (int i = 0; i < s2.length(); i++) {
            index = array2[i]-'a';

            // 等于0，表示该字符在s1中没有出现过
            if (0==chars[index]) {
                return false;
            }

            chars[index]--;
        }

        for (int val : chars) {
            if (0!=val) {
                return false;
            }
        }

        return true;
    }

    public static void main(String[] args) {
        System.out.println(new L0102().CheckPermutation("abc", "bca"));
        System.out.println(new L0102().CheckPermutation("abc", "bad"));
    }
}
