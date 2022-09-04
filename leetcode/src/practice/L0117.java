package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0117 {

    /**
     * 从指定节点开始，寻找下一层的第一个
     * @param from
     * @return
     */
    private Node findFirstChild(Node from) {

        while(null!=from) {
            if (null!=from.left) {
                return from.left;
            }

            if (null!=from.right) {
                return from.right;
            }

            from = from.next;
        }

        return null;
    }

    public Node connect(Node root) {
        if (null==root) {
            return null;
        }

        // 思路是当前层为下一层做事
        // 在每一层中，head执向最左侧
        Node head;

        // 顾名思义，表示当前正在处理的节点
        Node current;

        head = root;
        current = root;

        // 第一层while，每循环一次，表示表示处理一层
        while (null!=head) {

            // 第二层while，每循环一次，表示表示处理当前层的一个元素
            while (null!=current) {

                if(null!=current.left) {

                    if (null!=current.right) {
                        current.left.next = current.right;
                    } else {
                        current.left.next = findFirstChild(current.next);
                    }
                }

                if(null!=current.right) {
                    current.right.next = findFirstChild(current.next);
                }

                // 处理当前层的下一个
                current = current.next;
            }

            // 第二层while结束，意味着一层已经完成了，接下来进入下一层，
            head = findFirstChild(head);
            current = head;
        }

        return root;
    }

    public static void main(String[] args) {
        Node n1 = new Node(1);
        Node n2 = new Node(2);
        Node n3 = new Node(3);
        Node n4 = new Node(4);
        Node n5 = new Node(5);
        Node n6 = new Node(6);
        Node n7 = new Node(7);

        n1.left = n2;
        n1.right = n3;
        n2.left = n4;
        n2.right = n5;
        n3.left = n6;
        n3.right = n7;


//        Node node = new L0117().connect(n1);

//        System.out.println(node); // 3,9,20,null,null,15,7

        n1 = new Node(3);
        n2 = new Node(9);
        n3 = new Node(20);
        n6 = new Node(15);
        n7 = new Node(7);

        n1.left = n2;
        n1.right = n3;

        n3.left = n6;
        n3.right = n7;

        Node node = new L0117().connect(n1);

        System.out.println(node);
    }
}
