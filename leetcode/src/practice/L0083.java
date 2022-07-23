package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0083 {

    public ListNode deleteDuplicates(ListNode head) {
        // 异常处理
        if (null==head) {
            return null;
        }

        // 结果链表
        ListNode rlt = new ListNode();

        // 给结果链表追加第一个元素，后面的循环才好做比较
        rlt.next = head;

        // head指针也要往后移动
        head = head.next;

        // 结果链表的尾部
        ListNode rltTail = rlt.next;

        while (null!=head) {
            // 如果两个值不相等，就把head指向的节点放入结果链表尾部
            if (rltTail.val!= head.val) {
                rltTail.next = head;
                // 移动尾部指针
                rltTail = rltTail.next;
            }

            // head移动指针
            head = head.next;
        }

        // 注意：rltTail的内容来自head链表，所以其next可能指向下一个元素（例如3，3，只有第一个3会放入结果链表，而第一个3的next指向了第二个3，需要在此处理掉）
        rltTail.next = null;

        // 结果链表的头元素无效，其下一个元素才是head的
        return rlt.next;
    }


    public static void main(String[] args) {
        ListNode listNode = Tools.buildListNode(3,3);

        Tools.print(new L0083().deleteDuplicates(listNode));
    }
}
