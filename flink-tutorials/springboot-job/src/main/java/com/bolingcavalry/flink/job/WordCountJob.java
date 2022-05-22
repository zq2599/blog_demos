package com.bolingcavalry.flink.job;

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WordCountJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(WordCountJob.class);

    /**
     * parallelism(并行度)的优先级：代码中指定的 > Submit-Job时指定的 > flink全局配置(parallelism.default)
     */
    public void run() throws Exception {
        LOGGER.info("start run");
        //1、创建流处理的执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        String ncHost = "localhost";
        int ncPort = 7777;
        DataStream<String> inputDataStream = env.socketTextStream(ncHost, ncPort);
        //3、对数据流进行处理，具体来说就是将每行数据进行分词，收集<word,1>这样的二元组(最小粒度的二元组)
        DataStream<Tuple2<String,Integer>> resultStream = inputDataStream.flatMap(new WordTokenizer()).setParallelism(1)
                //这里的数字参数指的就是flink Tuple元组类型的泛型位置(见注释)
//                .keyBy(0) //区别于DataSet#groupBy(..)方法，因为数据流是一个一个来的，不像数据集那样一下就全部准备好了，所以从语义上讲，称作keyBy()
                .keyBy(new KeySelector<Tuple2<String, Integer>, Object>() {
                    @Override
                    public Object getKey(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
                        return stringIntegerTuple2.f0;
                    }
                })
                .sum(1)
                .setParallelism(1) //每个步骤都可以设置并行度,言外之意每个步骤都可以多线程执行
                ;
        //4、打印结果：parallelThreadIndex > (word,statedCount)
        //这个parallelThreadIndex指的就是print()并行线程的index下标(此处使用指定的并行度3)
        resultStream.print().setParallelism(1); //每个步骤都可以设置并行度,言外之意每个步骤都可以多线程执行
        //5、由于当前是流处理，需要手动触发这个任务(把数据流灌进来)，否则上面的print()是不起作用的
        env.execute();
        LOGGER.info("finish execute");
    }

}