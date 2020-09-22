package com.bolingcavalry.customize;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-03-14 22:08
 * @description 自定义的sink，写入mysql
 */
public class StudentSink {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //并行度为1
        env.setParallelism(1);

        List<Student> list = new ArrayList<>();
        list.add(new Student("aaa", 11));
        list.add(new Student("bbb", 12));
        list.add(new Student("ccc", 13));
        list.add(new Student("ddd", 14));
        list.add(new Student("eee", 15));
        list.add(new Student("fff", 16));

        env.fromCollection(list)
            .addSink(new MySQLSinkFunction())
            .disableChaining();

        env.execute("sink demo : customize mysql obj");
    }
}
