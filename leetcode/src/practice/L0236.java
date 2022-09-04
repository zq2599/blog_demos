package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0236 {

    private TreeNode res = null;

    // 思路，由于是要找最近的公共祖先，所以一开始要做的事情非常直接暴力：一口气捅到底！！！
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        dfs(root,p,q);
        return res;
    }

    private boolean dfs(TreeNode root, TreeNode p, TreeNode q) {
        // 如果到底了，就直接返回false
        if (null==root) {
            return false;
        }

        // 啥也不做，立即递归调用，目的就是一口气捅到底再说
        boolean lRlt = dfs(root.left, p, q);
        boolean rRlt = dfs(root.right, p, q);

        // 注意，这一段代码是判断当前节点是不是最近公共祖先，这段代码先不急着写，
        // 要把下面的处理自己这一层的逻辑的代码写完再来这里写，思路会更清晰

        // 要注意后序遍历的特点，就是从下往上逐层处理，所以第一次判断出公共祖先的root，一定是最近的公共祖先，
        // 所以，res为空才会找公共祖先，一旦res非空，就表示找到了，就再也不用找了
        if (null==res) {
            if (lRlt && rRlt) { // root左子树找到p，右子树找到q，那么root就是公共祖先
                res = root;
            } else if (root==p) { // root自己就是p，还能在左子树或者有子树找到一个，那么root就是公公祖先
                if (lRlt || rRlt) {
                    res = root;
                }
            } else if (root==q) { //
                if (lRlt || rRlt) {
                    res = root;
                }
            }
        }

        // 一旦找到了，后面就啥都不用做了，立即返回
        if(null!=res) {
            return false;
        }

        // 先写这里的代码，也就是处理自己这一层的逻辑
        // 1. 先判断自己，如果等于p或者q，立即返回true
        if (root==p || root==q) {
            return true;
        }

        // 2. 如果自己不是p或者q，就看下面一层返回的值，
        // 如果是true，就返回true，表示自己的子节点中出现了p或者q
        if (lRlt) {
            return true;
        }

        if (rRlt) {
            return true;
        }

        // 返回false，表示自己和自己的子节点都不存在p或者q
        return false;
    }


    public static void main(String[] args) {
        TreeNode t1 = new TreeNode(3);
        TreeNode t2 = new TreeNode(5);
        TreeNode t3 = new TreeNode(1);
        TreeNode t4 = new TreeNode(6);
        TreeNode t5 = new TreeNode(2);
        TreeNode t6 = new TreeNode(0);
        TreeNode t7 = new TreeNode(8);
        TreeNode t8 = new TreeNode(7);
        TreeNode t9 = new TreeNode(4);
        t1.left = t2;
        t1.right = t3;
        t2.left =t4;
        t2.right = t5;
        t3.left = t6;
        t3.right = t7;
        t5.left = t8;
        t5.right = t9;

        System.out.println((new L0236().lowestCommonAncestor(t1, t2,t3).val)); // 3
    }
}
