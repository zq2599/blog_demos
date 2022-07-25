package practice;

import java.util.Deque;
import java.util.LinkedList;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0919 {

    class CBTInserter {

        private TreeNode root;

        /**
         * 所有可以添加子节点的节点，都放在candidate中
         */
        private Deque<TreeNode> candidate = new LinkedList<>();


        public CBTInserter(TreeNode root) {
            this.root = root;

            // BFS基本操作，牢记这是基于队列的：入口在尾，出口在头
            Deque<TreeNode> queue = new LinkedList<>();

            // offer是尾部进
            queue.offer(root);

            // 基操来了，基于队列完成遍历，而且是广度优先的
            while (!queue.isEmpty()) {
                // poll是头部出
                TreeNode current = queue.poll();

                // 非空就入队列
                if (null!=current.left) {
                    queue.offer(current.left);
                }

                if (null!=current.right) {
                   queue.offer(current.right);
                }

                // 只要有一个子节点为空，就证明current可以添加子节点，就将其放入candidate中
                if (null==current.right || null==current.left) {
                    candidate.offer(current);
                }
            }

            // 此时小结一下：刚才遍历的顺序是从根节点向下一层一层遍历的，所以上层的先遍历到，所以上层的先入queue，
            // 所以上层的先从queue出来，所以上层的先进入candidate，
            // 如此以来，从candidata的头部读到的，就是上层的可以插入的节点，这就符合我们的要求了
        }

        public int insert(int val) {

            // 要新增的值包装成节点
            TreeNode child = new TreeNode(val);

            // 千万注意，candidate中取出的节点，有可能右子树为空，此时添加子节点后就满了，需要从candidate中移除，
            // 但也有可能左右子树都为空，那么添加到左子树之后，右子树还是空的，就不能从candidate中移除，
            // 因此，要用peek方法，这样只是取出来用而不会移除
            TreeNode parent = candidate.peek();

            if (null==parent.left) {
                // 如果左子树为空，那显然是左右都为空，
                // 左右都为空的时候，应该先添加到左边
                parent.left = child;
            } else {
                // 如果左子树非空，那么显然只剩下右子树为空了，
                parent.right = child;
                // 此刻parent的左右子树都有子节点了，就不能放在candidate中了，用poll方法移除
                candidate.poll();
            }

            // 最终别忘了，新增的节点左右子树都为空，可以添加子节点，所以要放入candidate
            candidate.offer(child);

            return parent.val;
        }

        public TreeNode get_root() {
            return root;
        }
    }





    public static void main( String[] args ) {

    }
}
