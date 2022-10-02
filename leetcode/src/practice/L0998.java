package practice;

/**
 * @program: leetcode
 * @description:
 * @author: za2599@gmail.com
 * @create: 2022-06-30 22:33
 **/
public class L0998 {

    public TreeNode insertIntoMaxTree(TreeNode root, int val) {
        if (null==root) {
            return null;
        }

        // 比根节点还大
        if (val>root.val) {
            TreeNode treeNode = new TreeNode(val);
            treeNode.left = root;
            return treeNode;
        }


        TreeNode current = root;
        TreeNode parent = null;

        while(null!=current) {
            if (current.val>val) {
                parent = current;
                current = current.right;
            } else {
                
                TreeNode treeNode = new TreeNode(val);
                
                // 新增一个节点treeNode，取代current的位置
                parent.right = treeNode;
                
                // 原current作为新增节点的左子树
                treeNode.left = current;
                
                return root;
            }    
        }

        // 代码能走到这里，就表示所有右节点都比val大，所以在叶子结点上新增一个右节点即可

        TreeNode treeNode = new TreeNode(val);
                
        // 新增一个节点treeNode，取代current的位置
        parent.right = treeNode;

        return root;
    }

    



    public static void main(String[] args) {   
        TreeNode t1 = new TreeNode(4);
        TreeNode t2 = new TreeNode(1); 
        TreeNode t3 = new TreeNode(3);
        TreeNode t4 = new TreeNode(2);
        
        t1.left = t2;
        t1.right = t3;
        t3.left = t4;
     
        Tools.print(t1); // 
        System.out.println("");
        Tools.print(new L0998().insertIntoMaxTree(t1, 5)); // 2
        System.out.println("");
        System.out.println("*************************************");
        


        t1 = new TreeNode(5);
        t2 = new TreeNode(2); 
        t3 = new TreeNode(4);
        t4 = new TreeNode(1);

        t1.left = t2;
        t1.right = t3;
        t2.right = t4;    

        Tools.print(t1); // 
        System.out.println("");
        Tools.print(new L0998().insertIntoMaxTree(t1, 3)); // 
        System.out.println("");
        System.out.println("*************************************");
        

        t1 = new TreeNode(5);
        t2 = new TreeNode(2); 
        t3 = new TreeNode(3);
        t4 = new TreeNode(1);

        t1.left = t2;
        t1.right = t3;
        t2.right = t4;    

        Tools.print(t1); // 
        System.out.println("");
        Tools.print(new L0998().insertIntoMaxTree(t1, 4)); // 
        System.out.println("");
        System.out.println("*************************************");

    }
}
