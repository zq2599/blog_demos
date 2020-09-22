package com.bolingcavalry.sparkdemo.app;


import com.bolingcavalry.sparkdemo.bean.PageInfo;
import com.bolingcavalry.sparkdemo.util.Tools;
import org.apache.commons.lang3.StringUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @Description: 根据wiki的统计来查找最高访问量的文章
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/2/8 17:21
 */
public class WikiRank {

    private static final Logger logger = LoggerFactory.getLogger(WikiRank.class);


    private static final int TOP = 100;

    public static void main(String[] args) {
        if(null==args
                || args.length<2
                || StringUtils.isEmpty(args[0])
                || StringUtils.isEmpty(args[1])) {
            logger.error("invalid params!");
        }


        String hdfsHost = args[0];
        String hdfsPort = args[1];

        SparkConf sparkConf = new SparkConf().setAppName("Spark WordCount Application (java)");

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConf);

        String hdfsBasePath = "hdfs://" + hdfsHost + ":" + hdfsPort;
        //文本文件的hdfs路径
        String inputPath = hdfsBasePath + "/input/*";

        //输出结果文件的hdfs路径
        String outputPath = hdfsBasePath + "/output/"
                + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

        logger.info("input path : {}", inputPath);
        logger.info("output path : {}", outputPath);

        logger.info("import text");
        //导入文件
        JavaRDD<String> textFile = javaSparkContext.textFile(inputPath);

        logger.info("do map operation");
        JavaPairRDD<String, PageInfo> counts = textFile
                //过滤掉无效的数据
                .filter((Function<String, Boolean>) v1 -> {
                    if(StringUtils.isBlank(v1)){
                        return false;
                    }

                    //分割为数组
                    String[] array = v1.split(" ");

                    /**
                     * 以下情况都要过滤掉
                     * 1. 名称无效（array[1]）
                     * 2. 请求次数无效(array[2)
                     * 3. 请求总字节数无效(array[3)
                     */
                    if(null==array
                            || array.length<4
                            || StringUtils.isBlank(array[1])
                            || !StringUtils.isNumeric(array[2])
                            || !StringUtils.isNumeric(array[3])){
                        logger.error("find invalid data [{}]", v1);
                        return false;
                    }

                    return true;
                })
                //将每一行转成一个PageInfo对象
                .map((Function<String, PageInfo>) v1 -> {
                    String[] array = v1.split(" ");

                    PageInfo pageInfo = new PageInfo();

                    try {
                        pageInfo.setName(URLDecoder.decode(array[1], "UTF-8"));
                    }catch (Exception e){
                        //有的字符串没有做过urlencode，此时做urldecode可能抛出异常(例如abc%)，此时用原来的内容作为name即可
                        pageInfo.setName(array[1]);
                    }

                    pageInfo.setUrl(Tools.getUrl(array[0], array[1]));

                    pageInfo.setRequestTimes(Integer.valueOf(array[2]));
                    pageInfo.setRequestLength(Long.valueOf(array[3]));
                    pageInfo.getRaws().add(v1);

                    return pageInfo;
                })
                //转成键值对，键是url，值是PageInfo对象
                .mapToPair(pageInfo -> new Tuple2<>(pageInfo.getUrl(), pageInfo))
                //按照url做reduce，将请求次数累加
                .reduceByKey((Function2<PageInfo, PageInfo, PageInfo>) (v1, v2) -> {
                    v2.setRequestTimes(v2.getRequestTimes() + v1.getRequestTimes());
                    v2.getRaws().addAll(v1.getRaws());
                    return v2;
                });

        logger.info("do convert");
        //先将key和value倒过来，再按照key排序
        JavaPairRDD<Integer, PageInfo> sorts = counts
                //key和value颠倒，生成新的map
                .mapToPair(tuple2 -> new Tuple2<>(tuple2._2().getRequestTimes(), tuple2._2()))
                //按照key倒排序
                .sortByKey(false);

        logger.info("take top " + TOP);
        //取前10个
        List<Tuple2<Integer, PageInfo>> top = sorts.take(TOP);

        StringBuilder sbud = new StringBuilder("top "+ TOP + " word :\n");

        //打印出来
        for(Tuple2<Integer, PageInfo> tuple2 : top){
            sbud.append(tuple2._2().getName())
                    .append("\t")
                    .append(tuple2._1())
                    .append("\n");
        }

        logger.info(sbud.toString());

        logger.info("merge and save as file");
        //分区合并成一个，再导出为一个txt保存在hdfs
        javaSparkContext
                .parallelize(top)
                .coalesce(1)
                .map(
                        tuple2 -> new Tuple2<>(tuple2._2().getRequestTimes(), tuple2._2().getName() + " ### " + tuple2._2().getUrl() +" ### " + tuple2._2().getRaws().toString())
                )
                .saveAsTextFile(outputPath);

        logger.info("close context");
        //关闭context
        javaSparkContext.close();
    }
}
