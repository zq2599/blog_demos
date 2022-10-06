package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L1309 {

    
    private int appendOnce(StringBuilder sbud, int index, char[] array) {
        if (array.length>2 && index<(array.length-2) && '#'==array[index+2]) {
            int val = 10*(array[index]-'0') + (array[index+1]-'0');
            char a = (char)('j'+(val-10));
            sbud.append(a);
            return 3;
        } 

        char a = (char)('a'+(array[index]-'1'));
        sbud.append(a);
        return 1;
    }

    public String freqAlphabets(String s) {
        int index = 0;
        StringBuilder sbud = new StringBuilder();
        char[] array = s.toCharArray();

        while(index<array.length) {
            index += appendOnce(sbud, index, array);
        }

        return sbud.toString();
    }

    public static void main(String[] args) {
        System.out.println(new L1309().freqAlphabets("10#11#12")); // jkab
        System.out.println(""); 

        System.out.println(new L1309().freqAlphabets("1326#")); // acz
        System.out.println("");
    }
}
