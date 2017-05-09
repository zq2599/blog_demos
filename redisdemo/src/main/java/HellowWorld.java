import java.util.HashMap;
import java.util.Map;

/**
 * @author boling_cavalry
 * @version V1.0
 * @Description: try gc
 * @email zq2599@gmail.com
 * @Date 17/4/28 上午10:38
 */
public class HellowWorld {

    private void useMemory(){
        Map<String, String> map = new HashMap<String, String>();
        for(int i=0;i<100;i++){
            map.put(new String(String.valueOf(i)), new String(String.valueOf(i)));
        }
    }

    public static void main(String[] args){
        HellowWorld.class.getMethods();
        System.out.println("start test");
        HellowWorld hellowWorld = new HellowWorld();

        for(int i=0;i<10;i++){
            hellowWorld.useMemory();
        }

        System.out.println("end test");

    }
}
