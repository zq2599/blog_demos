package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0021 {

    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        // 异常处理之一：list1为空，返回list2
        if (null==list1) {
            return list2;
        }

        // 异常处理之二：list2为空，返回list1
        if (null==list2) {
            return list1;
        }

        // 现在，只需要处理list1和list2都不为空的情况
        // 返回值是个新的链表
        ListNode rlt = new ListNode();

        // 返回链表的最后一个元素
        ListNode rltTail = rlt;

        // 一旦有一个指针移动到尾部，就不需要再比较了，while就结束了
        while (null!=list1 && null!=list2) {
            // 比较两个指针指向的对象的值，将小的放入返回链表，并且指针往后移动一次
            if(list1.val<=list2.val) {
                // 让返回链表的尾部连接到一号指针指向的元素
                rltTail.next = list1;
                // 一号指针指向下一个
                list1 = list1.next;
            } else {
                // 让返回链表的尾部连接到二号指针指向的元素
                rltTail.next = list2;
                // 二号指针指向下一个
                list2 = list2.next;
            }

            // 刚刚新增一条记录，尾部指针往后移动
            rltTail = rltTail.next;
        }

        // 如果一号链表已经遍历完成，那么此时只要将返回链表的指正连接到二号链表即可
        if(null==list1) {
            rltTail.next = list2;
        } else {
            rltTail.next = list1;
        }

        // 注意rlt的第一个节点是new出来的，不是链表1或者链表2的内容，所以返回的时候，要从rlt.next返回
        return rlt.next;
    }


    public static void main(String[] args) {
        ListNode list1 = new ListNode(1);

        list1.next = new ListNode(2);
        list1.next.next = new ListNode(4);

        ListNode list2 = new ListNode(1);

        list2.next = new ListNode(3);
        list2.next.next = new ListNode(4);

        Tools.print(new L0021().mergeTwoLists(list1, list2));
    }
}
