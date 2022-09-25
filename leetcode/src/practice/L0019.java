package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0019 {

    /*
    public ListNode removeNthFromEnd(ListNode head, int n) {
        ListNode[] array = new ListNode[30];
        ListNode current = head;
        int num = 0;

        while (null!=current) {
            array[num++] = current;
            current = current.next;
        }

        if (1==num) {
            return null;
        } else if (n==num) {
            // 本题的主算法思路是将前一个节点的next指向被删除节点的下一个节点，
            // 所以遇到删除头结点的场景就不合适了，需要特殊处理，直接返回第二个节点即可
            return array[1];
        }

        array[num-n-1].next = array[num-n-1].next.next;

        return head;
    }
    */

    public ListNode removeNthFromEnd(ListNode head, int n) {
        ListNode fast = head;
        ListNode slow = head;
        ListNode prev = head;
        int num = 0;

        for (int i = 0; i < n; i++) {
            if (null!=fast.next) {
                prev = fast;
            }

            fast = fast.next;
            num++;
        }

        while (null!=fast) {
            if (null!=fast.next) {
                prev = fast;
            }
            fast = fast.next;
            slow = slow.next;
            num++;
        }

        // 只有一个，删完就空了
        if (1==num) {
            return null;
        }

        // 删尾巴
        if (1==n) {
            prev.next = null;
            return head;
        }


        slow.val = slow.next.val;
        slow.next = slow.next.next;

        return head;
    }






    public static void main(String[] args) {
        ListNode l1 = new ListNode(1);
        ListNode l2 = new ListNode(2);
        ListNode l3 = new ListNode(3);
        ListNode l4 = new ListNode(4);
        ListNode l5 = new ListNode(5);
        l1.next = l2;
        l2.next = l3;
        l3.next = l4;
        l4.next = l5;

//        Tools.print(new L0019().removeNthFromEnd(l1, 2)); // 1,2,3,5
//
//        l1 = new ListNode(1);
//        Tools.print(new L0019().removeNthFromEnd(l1, 1)); // 1

//        l1 = new ListNode(1);
//        l2 = new ListNode(2);
//        l1.next = l2;
//        Tools.print(new L0019().removeNthFromEnd(l1, 1)); // 1
//
//        l1 = new ListNode(1);
//        l2 = new ListNode(2);
//        l1.next = l2;
//        Tools.print(new L0019().removeNthFromEnd(l1, 2)); // 1

        l1 = new ListNode(1);
        l2 = new ListNode(2);
        l3 = new ListNode(3);
        l1.next = l2;
        l2.next = l3;
        Tools.print(new L0019().removeNthFromEnd(l1, 3)); // 2,3
    }
}
