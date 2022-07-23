package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0206 {

    public ListNode reverseList(ListNode head) {
        // 异常处理
        if(null==head || null==head.next) {
            return head;
        }

        // 输入是 1 -> 2 -> 3
        // 反转后 1 <- 2 <- 3

        // 前一个元素的指针，从head开始
        ListNode prev = head;

        // 当前元素的指针，从head的下一个开始
        ListNode cur = head.next;

        ListNode next;

        while(cur!=null) {
            // 1. 将当前元素的next保存起来，稍后当前元素的指针移动会用到
            next = cur.next;
            // 2. 当前元素的next指向上一个，这就是反转
            cur.next = prev;
            // 3. 前一个元素的指针移动一次
            prev = cur;
            // 4. 当前指针移动一次，由于cur.next已经指向了前一个元素，所以不能用cur.next，应该用next
            cur = next;
        }

        // 此时，原本的头成了尾巴，其next应该为null（原本指向的是下一个元素），
        // 如果不执行下面的操作，head.next指向第二个元素，而第二个元素因为反转，其next又指向了head，这就产生了环，遍历就会出问题
        head.next = null;

        // cur移动到null了，所以真正的最后一个元素是prev,
        // 执行了反转操作后，prev成了头元素
        return prev;
    }


    public static void main(String[] args) {
        Tools.print(new L0206().reverseList(Tools.buildListNode(1,2,3,4,5)));
    }
}
