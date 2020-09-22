package question002;

/**
 * @Description: (这里用一句话描述这个类的作用)
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/1/6 16:15
 */
public class Solution {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode result = new ListNode(l1.val + l2.val);
        ListNode current = result;
        ListNode currentLeft = l1;
        ListNode currentRight = l2;
        boolean addFlag = false;

        while(true){

            if(addFlag){
                current.val += 1;
            }

            if(current.val >9){
                addFlag = true;
                current.val -= 10;
            }else{
                addFlag = false;
            }

            if(null==currentLeft.next && null==currentRight.next){
                if(addFlag){
                    current.next = new ListNode(1);
                }
                return result;
            }

            currentLeft = null==currentLeft.next ? new ListNode(0) : currentLeft.next;
            currentRight = null==currentRight.next ? new ListNode(0) : currentRight.next;

            current.next = new ListNode(currentLeft.val + currentRight.val);
            current = current.next;
        }
    }

    public static void main(String[] args){
        ListNode l1 = new ListNode(1);
        ListNode l11 = new ListNode(8);
        ListNode l12 = new ListNode(3);

        l1.next = l11;
        //l11.next = l12;

        ListNode l2 = new ListNode(0);
        ListNode l21 = new ListNode(6);
        ListNode l22 = new ListNode(4);

        //l2.next = l21;
        //l21.next = l22;

        ListNode rlt = new Solution().addTwoNumbers(l1, l2);

        while (null!=rlt){
            System.out.println(rlt.val);
            rlt = rlt.next;
        }
    }
}

