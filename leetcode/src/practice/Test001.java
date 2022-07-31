package practice;

public class Test001 {

    public static int[] isPrime = new int[100001];
    public static int[] primes = new int [100001];
    static int primeNum = 0;

    public static void main(String[] args) {
        for(int i=2;i<=100000;i++) {
            if(isPrime[i]==0) {
                // i是素数，就放入primes数组中
                primes[primeNum] = i;
                // 更新primes中素数的数量
                primeNum++;
            }

            for(int j=0;primes[j]*i<=100000;j++) {
                // primes[j]*i的结果是个乘积，这样的数字显然不是素数，所以在isPrimes数组中标注为1
                isPrime[primes[j]*i] = 1;

                // 如果i是primes中某个素数的倍数，就没有必要再计算了，退出算下一个，
                // 例如i=8的时候，其实在之前i=4时就已经计算出8不是素数了
                if(i%primes[j]==0) {
                    break;
                }

            }
        }

        System.out.print("{");
        for(int i=0;i<primeNum;i++) {
            System.out.print(primes[i] + ", ");
        }
        System.out.print("}");


//        System.out.println(primeNum);
    }

}
