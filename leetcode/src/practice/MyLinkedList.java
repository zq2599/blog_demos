package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class MyLinkedList {

    class Node {

        Node(int val) {
            this.val = val;
        }

        int val;
        Node next;
    }

    private Node head = null;
    private Node tail = null;

    /**
     * 节点数量，即链表长度
     */
    private int len = 0;

    public MyLinkedList() {
        super();
    }

    public int get(int index) {
        if (0==len || index>=len) {
            return -1;
        }

        Node current = head;

        while(index>0) {
            current = current.next;
            index--;
        }

        return current.val;
    }

    public void addAtHead(int val) {
        Node node = new Node(val);

        if (null==head) {
            // 只有一个值的时候，既是头也是尾
            head = node;
            tail = node;
        } else {
            node.next = head;
            head = node;
        }

        len++;
    }

    public void addAtTail(int val) {
        Node node = new Node(val);

        if (null==tail) {
            // 只有一个值的时候，既是头也是尾
            head = node;
        } else {
            tail.next = node;
        }
        tail = node;

        len++;
    }

    public void addAtIndex(int index, int val) {

        if (index<=0) {
            // 如果index小于0，则在头部插入节点
            addAtHead(val);
        } else if (index==len) {
            // 如果 index 等于链表的长度，则该节点将附加到链表的末尾
            addAtTail(val);
        } else if (index>len) {
            // 如果 index 大于链表长度，则不会插入节点
            return;
        } else {
            Node node = new Node(val);

            Node prev = null;
            Node current = head;

            while(index>0) {
                prev = current;
                current = current.next;
                index--;
            }

            prev.next = node;
            node.next = current;
            // 节点总数加一
            len++;
        }
    }

    public void deleteAtIndex(int index) {

        if (index>=len) {
            return;
        }

        if (0==index) {
            head = head.next;
        } else {
            Node prev = null;
            Node current = head;

            while(index>0) {
                prev = current;
                current = current.next;
                index--;
            }

            // 表示本次删除的是末尾元素
            if (null==current.next) {
                tail = prev;
                prev.next = null;
            } else {
                prev.next = current.next;
            }
        }

        len--;
    }

    private void check() {

        int num = 0;

        Node node = head;

        while (null!=node) {
            if (-1!=node.val) {
                num++;
            }
            node = node.next;
        }

        System.out.println("len [" + len + ", num [" + num + "]");

    }



    public static void main(String[] args) {
        MyLinkedList myLinkedList = new MyLinkedList();


        myLinkedList.addAtHead(1);
        myLinkedList.addAtTail(3);
        myLinkedList.addAtIndex(1,2);   //链表变为1-> 2-> 3
        System.out.println(myLinkedList.get(1));            //返回2
        myLinkedList.deleteAtIndex(1);  //现在链表是1-> 3
        System.out.println(myLinkedList.get(1));            //返回3

        myLinkedList = new MyLinkedList();

        myLinkedList.addAtHead(1);
        myLinkedList.addAtTail(3);
        myLinkedList.addAtIndex(1,2);   //链表变为1-> 2-> 3
        System.out.println(myLinkedList.get(1));            //返回2
        myLinkedList.deleteAtIndex(1);  //现在链表是1-> 3
        System.out.println(myLinkedList.get(1));            //返回3

        /*
        linkedList.addAtHead(1);
        linkedList.deleteAtIndex(0);
         */

        /*
        linkedList.addAtHead(7);
        linkedList.addAtHead(2);
        linkedList.addAtHead(1);
        linkedList.addAtIndex(3,0);
        linkedList.deleteAtIndex(2);
        linkedList.addAtHead(6);
        linkedList.addAtTail(4);
        linkedList.get(4);
        linkedList.addAtHead(4);
        linkedList.addAtIndex(5,0);
        linkedList.addAtHead(6);
        */

        myLinkedList.addAtTail(1);
        System.out.println(myLinkedList.get(0));


        myLinkedList = new MyLinkedList();
        myLinkedList.addAtHead(84);
        myLinkedList.addAtTail(2);
        myLinkedList.addAtTail(39);
        myLinkedList.get(3);
        myLinkedList.get(1);
        myLinkedList.addAtTail(42);
        myLinkedList.addAtIndex(1,80);
        myLinkedList.addAtHead(14);
        myLinkedList.addAtHead(1);
        myLinkedList.addAtTail(53);
        myLinkedList.addAtTail(98);
        myLinkedList.addAtTail(19);
        myLinkedList.addAtTail(12);
        myLinkedList.get(2);
        myLinkedList.addAtHead(16);
        myLinkedList.addAtHead(33);
        myLinkedList.addAtIndex(4,17);
        myLinkedList.addAtIndex(6,8);
        myLinkedList.addAtHead(37);
        myLinkedList.addAtTail(43);
        myLinkedList.deleteAtIndex(11);
        myLinkedList.addAtHead(80);
        myLinkedList.addAtHead(31);
        myLinkedList.addAtIndex(13,23);
        myLinkedList.addAtTail(17);
        myLinkedList.get(4);
        myLinkedList.addAtIndex(10,0);
        myLinkedList.addAtTail(21);
        myLinkedList.addAtHead(73);
        myLinkedList.addAtHead(22);
        myLinkedList.addAtIndex(24,37);
        myLinkedList.addAtTail(14);
        myLinkedList.addAtHead(97);
        myLinkedList.addAtHead(8);
        myLinkedList.get(6);
        myLinkedList.deleteAtIndex(17);
        myLinkedList.addAtTail(50);
        myLinkedList.addAtTail(28);
        myLinkedList.addAtHead(76);
        myLinkedList.addAtTail(79);
        myLinkedList.get(18);
        System.out.println("bbb");
        myLinkedList.deleteAtIndex(30);
        System.out.println("bbb");

        myLinkedList.addAtTail(5);
        myLinkedList.addAtHead(9);
        myLinkedList.addAtTail(83);
        myLinkedList.deleteAtIndex(3);
        myLinkedList.addAtTail(40);
        myLinkedList.deleteAtIndex(26);
        myLinkedList.addAtIndex(20,90);
        myLinkedList.deleteAtIndex(30);




        myLinkedList.addAtTail(40);
        myLinkedList.addAtHead(56);
        myLinkedList.addAtIndex(15,23);
        myLinkedList.addAtHead(51);
        myLinkedList.addAtHead(21);
        myLinkedList.get(26);
        myLinkedList.addAtHead(83);
        myLinkedList.get(30);
        myLinkedList.addAtHead(12);
        myLinkedList.deleteAtIndex(8);
        myLinkedList.get(4);
        myLinkedList.addAtHead(20);
        myLinkedList.addAtTail(45);
        myLinkedList.get(10);
        myLinkedList.addAtHead(56);
        myLinkedList.get(18);
        myLinkedList.addAtTail(33);
        myLinkedList.get(2);
        myLinkedList.addAtTail(70);
        myLinkedList.addAtHead(57);
        myLinkedList.addAtIndex(31,24);
        myLinkedList.addAtIndex(16,92);
        myLinkedList.addAtHead(40);
        myLinkedList.addAtHead(23);
        myLinkedList.deleteAtIndex(26);
        myLinkedList.get(1);
        myLinkedList.addAtHead(92);
        myLinkedList.addAtIndex(3,78);
        myLinkedList.addAtTail(42);
        myLinkedList.get(18);
        myLinkedList.addAtIndex(39,9);
        myLinkedList.get(13);
        myLinkedList.addAtIndex(33,17);
        myLinkedList.get(51);
        myLinkedList.addAtIndex(18,95);
        myLinkedList.addAtIndex(18,33);
        myLinkedList.addAtHead(80);
        myLinkedList.addAtHead(21);
        myLinkedList.addAtTail(7);
        myLinkedList.addAtIndex(17,46);
        myLinkedList.get(33);
        myLinkedList.addAtHead(60);
        myLinkedList.addAtTail(26);
        myLinkedList.addAtTail(4);
        myLinkedList.addAtHead(9);
        myLinkedList.get(45);
        myLinkedList.addAtTail(38);
        myLinkedList.addAtHead(95);
        myLinkedList.addAtTail(78);
        myLinkedList.get(54);
        myLinkedList.addAtIndex(42,86);


        myLinkedList = new MyLinkedList();
        myLinkedList.addAtTail(1);
        myLinkedList.addAtTail(3);
        System.out.println(myLinkedList.get(1));

    }
}
