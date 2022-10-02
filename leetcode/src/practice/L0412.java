package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0412 {

    public List<String> fizzBuzz(int n) {
        List<String> rlt = new ArrayList<>(n);     
        
        boolean is3;
        boolean is5;

        for (int i=1;i<=n;i++) {
            is3 = 0==i%3;
            is5 = 0==i%5;
            
            if (is3 && is5) {
                rlt.add("FizzBuzz");
                continue;
            }
    
            if (is3) {
                rlt.add("Fizz");
                continue;
            }
    
            if (is5) {
                rlt.add("Buzz");
                continue;
            }

            rlt.add(String.valueOf(i));
        }

        return rlt;
    }

    public static void main(String[] args) {
        System.out.println(new L0412().fizzBuzz(3)); // "1","2","Fizz"
        System.out.println(new L0412().fizzBuzz(5)); // "1","2","Fizz","4","Buzz"
        System.out.println(new L0412().fizzBuzz(15)); // "1","2","Fizz","4","Buzz","Fizz","7","8","Fizz","Buzz","11","Fizz","13","14","FizzBuzz"

        System.out.println("");
    }
}
