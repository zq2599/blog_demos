package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0876 {

    public ListNode middleNode(ListNode head) {
        // 异常处理
        if (null==head || null==head.next) {
            return head;
        }

        ListNode slow = head;

        while(null!=head && null!=head.next) {
            // 快指针移动两步
            head = head.next.next;
            // 慢指针移动一步
            slow = slow.next;
        }

        return slow;
    }

    public static void main(String[] args) {
        System.out.println(new L0876().middleNode(Tools.buildListNode(1,2,3,4,5,6)).val);
    }
}
