package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0142 {

    public ListNode detectCycle(ListNode head) {
        // 异常处理
        if (null==head || null==head.next) {
            return null;
        }

        boolean hasCycle = false;

        ListNode fast = head;
        ListNode slow = head;

        while (null!=fast && null!=fast.next) {
            fast = fast.next.next;
            slow = slow.next;

            // 发现环，就退出while循环
            if (fast==slow) {
                hasCycle = true;
                break;
            }
        }

        // 没有环就立即返回null
        if (!hasCycle) {
            return null;
        }

        // 根据算式推导出：从相遇位置和定点同时出发，两个指针相遇地点就是环形入口处
        while (head!=slow) {
            head = head.next;
            slow = slow.next;
        }

        return slow;
    }


    public static void main(String[] args) {
        ListNode l0 = new ListNode(3);
        ListNode l1 = new ListNode(2);
        ListNode l2 = new ListNode(0);
        ListNode l3 = new ListNode(-4);

        l0.next = l1;
        l1.next = l2;
        l2.next = l3;
        l3.next = l1;

        ListNode rlt = new L0142().detectCycle(l0);

        System.out.println(null==rlt ? null : rlt.val);
    }
}
