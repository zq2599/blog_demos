package interview;

public class L0801 {

    public int waysToStep(int n) {

        if (1 == n) {
            return 1;
        } else if (2==n) {
            return 2;
        } else if (3==n) {
            return 4;
        }

        int l1 = 1;
        int l2 = 2;
        int l3 = 4;

        int current = 0;

        for (int i=4;i<=n;i++) {
            // 千万注意，一次相加就可能出现负数，所以不能直接l3+l2+l1，要每加一次取模一次
            current = ((l3 + l2) % 1000000007 + l1) % 1000000007;
            l1 = l2;
            l2 = l3;
            l3 = current;
        }

        return current;
    }

    public static void main(String[] args) {
        System.out.println(new L0801().waysToStep(61));
    }
}
