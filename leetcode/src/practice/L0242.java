package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0242 {

    /*
    public boolean isAnagram(String s, String t) {
        if (s.length()!=t.length()) {
            return false;
        }

        int[] nums = new int[26];

        char[] array = s.toCharArray();

        for (int i=0;i<array.length;i++) {
            nums[array[i]-'a']++;
        }

        array = t.toCharArray();

        for (int i=0;i< array.length;i++) {
            nums[array[i]-'a']--;
        }

        for(int i : nums) {
            if (0!=i) {
                return false;
            }
        }

        return true;
    }
    */

    public boolean isAnagram(String s, String t) {
        if (s.length()!=t.length()) {
            return false;
        }

        int[] nums = new int[26];

        char[] arrayS = s.toCharArray();
        char[] arrayT = t.toCharArray();

        for (int i=0;i<arrayS.length;i++) {
            nums[arrayS[i]-'a']++;
            nums[arrayT[i]-'a']--;
        }

        for(int i : nums) {
            if (0!=i) {
                return false;
            }
        }

        return true;
    }



    public static void main(String[] args) {
        System.out.println(new L0242().isAnagram("anagram","nagaram")); // true
        System.out.println(new L0242().isAnagram("rat","car\"")); // false
    }
}

