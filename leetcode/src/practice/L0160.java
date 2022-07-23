package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0160 {

    public ListNode getIntersectionNode(ListNode headA, ListNode headB) {
        // 异常处理
        if (null == headA || null == headB) {
            return null;
        }

        int lenA = 0, lenB = 0;

        ListNode pA = headA;
        ListNode pB = headB;

        // 一次循环，得到A和B链表的长度
        while (null!=pA || null!=pB){
            if (null!=pA) {
                lenA++;
                pA = pA.next;
            }

            if (null!=pB) {
                lenB++;
                pB = pB.next;
            }
        }

        pA = headA;
        pB = headB;

        if (lenA>lenB) {
            for(int i=0;i<(lenA-lenB);i++) {
                pA = pA.next;
            }
        } else {
            for (int i=0;i<(lenB-lenA);i++) {
                pB = pB.next;
            }
        }

        // 现在是同一起跑线了
        while (pA!=pB) {
            pA = pA.next;
            pB = pB.next;
        }

        return pA;
    }


    public static void main(String[] args) {


//        ListNode c0 = new ListNode(1);
//        ListNode c1 = new ListNode(8);
//        ListNode c2 = new ListNode(4);
//        ListNode c3 = new ListNode(5);
//
//        c0.next = c1;
//        c1.next = c2;
//        c2.next = c3;
//
//        ListNode a0 = new ListNode(4);
//        a0.next = c0;
//
//
//        ListNode b0 = new ListNode(5);
//        ListNode b1 = new ListNode(6);
//
//        b0.next = b1;
//        b1.next = c0;
//
//        Tools.print(a0);
//        System.out.println("");
//        Tools.print(b0);
//        System.out.println("");
//        Tools.print(new L0160().getIntersectionNode(a0, b0));

//        ListNode a0 = new ListNode(3);
//
//        ListNode b0 = new ListNode(2);
//        b0.next = a0;


        Tools.print(new L0160().getIntersectionNode(Tools.buildListNode(2,6,4), Tools.buildListNode(1,5)));
    }
}
