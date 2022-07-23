package practice;

/**
 * 快速排序
 * 通过一趟排序将待排序记录分割成独立的两部分，其中一部分记录的关键字均比另一部分关键字小，
 * 则分别对这两部分继续进行排序，直到整个序列有序。
 */
public class QuickSort {
    private static void show(int val) {
        for(int i=31;i>=0;i--){
            System.out.print((val & 1 << i) == 0 ? "0":"1");
        }
        System.out.println("");
    }


    public static void main(String[] args) {
        System.out.println(3&2);
    }
}

