package practice;

import java.util.*;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L01408 {

    /*
    public List<String> stringMatching(String[] words) {
        if(1==words.length) {
            return new ArrayList<>();
        }

        // map的设计：key是字符串长度，value是该长度的字符串的数组下标
        // 例如题目输入是["mass","as","hero","superhero"]
        // 那么字符串长度分别是：4，2，4，9
        // 所以map有三个key:4,2,9
        // 每个key对应的值如下：
        // 2 : [1] --这里的1是"as"在words中的数组下标
        // 4 : [0,2] --这里的0是"mass"在words中的数组下标，2是"hero"在words中的数组下标
        // 9 : [3] --这里的3是"superhero"在words中的数组下标
        Map<Integer, List<Integer>> map = new HashMap<>();
        Set<Integer> set = new HashSet<>();
        Set<String> outputs = new HashSet<>();

        for (int i=0;i<words.length;i++) {
            int len = words[i].length();
            map.computeIfAbsent(len, key -> new ArrayList<>()).add(i);
            set.add(len);
        }

        // 如果只有一种长度，就不存在子串的说法了
        if (set.size()<2) {
            return new ArrayList<>();
        }


        List<Integer> lens = new ArrayList<>(set);

        Collections.sort(lens);

        int size = lens.size();

        for(int i=0;i<(size-1);i++) {
            // 当前长度
            int len = lens.get(i);
            // 长度为len的字符串有哪些(words中的数组下标)
            List<Integer> currentLenStrs = map.get(len);

            for(int index : currentLenStrs) {

                // 假设一共有 1,2,3这三种长度，当前i=0的话，就表示要用长度为1的字符串去检查是不是长度为2和3的字符串的字串
                for (int j=(i+1);j<size;j++) {
                    // 要比较的长度是当前长度之后的
                    int toCheckLen = lens.get(j);
                    // 取出所有该长度的字符串数组下标
                    List<Integer> toCheckIndexs = map.get(toCheckLen);

                    for (int toCheckIndex : toCheckIndexs) {
                        if (words[toCheckIndex].contains(words[index])) {
                            outputs.add(words[index]);
                        }
                    }
                }
            }

        }

        return new ArrayList<>(outputs);
    }
    */

    /*
    public List<String> stringMatching(String[] words) {
        Arrays.sort(words, (o1, o2) -> {
            int a = o1.length();
            int b = o2.length();

            if(a>b) {
                return 1;
            } else if (a==b) {
                return 0;
            } else {
                return -1;
            }
        });

        List<String> output = new ArrayList<>();
        for(int i=0;i<(words.length-1);i++) {
            for(int j=i+1;j< words.length;j++) {
                if(words[j].contains(words[i])) {
                    output.add(words[i]);
                    // words[i]已经输出了，不用再用它去比较了
                    break;
                }
            }
        }

        return output;
    }
    */
    public List<String> stringMatching(String[] words) {
        List<String> output = new ArrayList<>();
        for(int i=0;i<words.length;i++) {
            for(int j=0;j<words.length;j++) {
                if(i!=j && words[j].contains(words[i])) {
                    output.add(words[i]);
                    // words[i]已经输出了，不用再用它去比较了
                    break;
                }
            }
        }

        return output;
    }

    public static void main(String[] args) {
//        System.out.println(new L01408().stringMatching(new String[] {"mass","as","hero","superhero"}));
//        System.out.println(new L01408().stringMatching(new String[] {"leetcode","et","code"}));
//        System.out.println(new L01408().stringMatching(new String[] {"blue","green","bu"}));
        System.out.println(new L01408().stringMatching(new String[] {"o","oo","zwhcxovmoplsln","yazwhcxovmoplslnx","vpxblpimwgfkwa","vpxblpimwgfkwans","usdhmxhfrlwfltl","eyazwhcxovmoplslnx","cgorzdq","vpxblpimwgfkwanswc","kg","bzwhcxovmoplslnv","rmtpys","vooi","azbjh","usdhmxhfrlwfltles","shuf","dzshuf","cshhrgelsutx","jazbjhy","vyjoncbgie","chjazbjhyl","wlkh","scgorzdq","iqp","vooic","qnvbdnw","jvpxblpimwgfkwansh","x","ngrmtpyscx","fabqxfujqsfznzqm","oeyazwhcxovmoplslnxrf","sj","kov","qsibppmta","qlusdhmxhfrlwfltlrn","ixteymfvnhwosyb","ryazwhcxovmoplslnx","sslcdzx","sjcw","iycfrmy","toooc","jyoubsh","zjvpxblpimwgfkwanshk","hcekmomliwbstq","mkovl","jhnmwchkn","dascgorzdqa","h","iycfrmypv","ynotnzdvsg","ih","sit","oyrmtpysgc","uesetymk","hjhnmwchknns","lxhbzp","kgo","nadnivlyu","yysitix","xgc","ffabqxfujqsfznzqm","on","jxgcfk","dogkjvogu","fkgofp","pdpqgnbktozhvux","dzshufoh","rrusakafjyflnxqu","fksslcdzx","hy","ftazbjh","are","ihtooocvb","xwqaokvgw","vooicc","ixyvcdysxerhmqh","jxyd","iycs","jrrusakafjyflnxqulr","fqgndznyygwiwxi","nbixteymfvnhwosyb","cfayidbgkclomg","bfdzshuf","jull","amngrmtpyscxqw","cspxujppyvxyjtk","fuscgorzdqa","onh","mbjrrusakafjyflnxqulru"}));
    }
}
