import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

public class GenerateESAggSortData {

    /**
     * 随机生成name字段的值，1-30
     * @return
     */
    private static String getName(){
        Random ra =new Random();
        return  String.valueOf(ra.nextInt(30) + 1);
    }

    /**
     * 随机生成value字段的值，1-1000
     * @return
     */
    private static int getValue(){
        Random ra =new Random();
        return  ra.nextInt(1000) + 1;
    }

    public static void main(String[] args) throws Exception{
        Map<String, SortBean> mapA = new HashMap<>();
        Map<String, SortBean> mapB = new HashMap<>();
        Map<String, SortBean> mapAll = new HashMap<>();

        //生成的批量导入脚本文件的路径
        File file =new File("d://bulk.json");

        if(file.exists()){
            file.delete();
        }

        file.createNewFile();

        FileOutputStream fop = new FileOutputStream(file);

        //一共一万个文档
        for(int i=0;i<10000;i++){
            Map<String, SortBean> map;
            String routing;

            //前一千个的routing为a，数据会落在一号分片
            //后面九千个的routing为b，数据会落在二号分片
            if(i<1000){
                routing = "a";
                map = mapA;
            }else{
                routing = "b";
                map = mapB;
            }

            String name = getName();
            int value = getValue();

            //存入mapA或者mapB
            SortBean sortBean = null;
            if(map.containsKey(name)){
                sortBean = map.get(name);
            } else {
                sortBean = new SortBean();
                sortBean.name = name;
                map.put(name, sortBean);
            }

            sortBean.value += value;


            //存入mapAll
            SortBean sortBeanOfAll = null;
            if(mapAll.containsKey(name)){
                sortBeanOfAll = mapAll.get(name);
            } else {
                sortBeanOfAll = new SortBean();
                sortBeanOfAll.name = name;
                mapAll.put(name, sortBeanOfAll);
            }

            sortBeanOfAll.value += value;


            fop.write(String.format("{ \"index\": {\"routing\":\"%s\"}}\n", routing).getBytes());
            fop.write(String.format("{ \"name\" : \"%s\", \"value\" : %d }\n", name, value).getBytes());
        }

        fop.flush();
        fop.close();

        List<SortBean> listA = new ArrayList<>(mapA.values());
        Collections.sort(listA);

        System.out.println("shardA : ");

        for(SortBean sortBean : listA){
            System.out.println(sortBean.name + " : " + sortBean.value);
        }

        List<SortBean> listB = new ArrayList<>(mapB.values());
        Collections.sort(listB);

        System.out.println("\n\n\nshardB : ");

        for(SortBean sortBean : listB){
            System.out.println(sortBean.name + " : " + sortBean.value);
        }



        List<SortBean> listAll = new ArrayList<>(mapAll.values());
        Collections.sort(listAll);

        System.out.println("\n\n\nTotal : ");

        for(SortBean sortBean : listAll){
            System.out.println(sortBean.name + " : " + sortBean.value);
        }
    }
}

class SortBean implements Comparable<SortBean> {

    String name;

    int value = 0;

    @Override
    public int compareTo(SortBean o) {
        return this.value>o.value ? -1 : 1;
    }
}
