package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0116 {

    List<List<Node>> res = new ArrayList<>();

    public Node connect(Node root) {
        if (null==root) {
            return null;
        }
        travel(root, 0);
        return root;
    }

    private void travel(Node root, int depth) {

        List<Node> list;

        if (res.size()<(depth+1)) {
            list = new ArrayList<>();
            res.add(list);
        } else {
            list = res.get(depth);
            list.get(list.size()-1).next = root;
        }

        list.add(root);

        if (null==root.left) {
            return;
        }

        travel(root.left, depth+1);
        travel(root.right, depth+1);
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


        Node node = new L0116().connect(n1);

        System.out.println(node); // 3,9,20,null,null,15,7
    }
}
