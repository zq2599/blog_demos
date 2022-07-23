package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0141 {

    public boolean hasCycle(ListNode head) {
        // 异常处理
        if (null==head || null==head.next) {
            return false;
        }

        ListNode slow = head;

        while (null!=head && null!=head.next) {
            head = head.next.next;
            slow = slow.next;

            if (head==slow) {
                return true;
            }
        }

        return false;
    }


    public static void main(String[] args) {
        ListNode l0 = new ListNode(3);
        ListNode l1 = new ListNode(2);
        ListNode l2 = new ListNode(0);
        ListNode l3 = new ListNode(-4);

        l0.next = l1;
        l1.next = l2;
        l2.next = l3;
        l3.next = l2;

        System.out.println(new L0141().hasCycle(l0));
    }
}
