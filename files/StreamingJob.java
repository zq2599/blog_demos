package com.bolingcavalry;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

public class StreamingJob {

	public static void main(String[] args) throws Exception {
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		DataStreamSource<String> text = env.socketTextStream("127.0.0.1", 18081, "\n");

		DataStream<WordWithCount> windowCount = text.flatMap(new FlatMapFunction<String, WordWithCount>() {
			public void flatMap(String value, Collector<WordWithCount> out) throws Exception {
				String[] splits = value.split("\\s");
				for (String word:splits) {
					out.collect(new WordWithCount(word,1L));
				}
			}
		})
				.keyBy("word")
				.timeWindow(Time.seconds(5),Time.seconds(1))
				.sum("count");
		windowCount.print().setParallelism(1);
		env.execute("Flink Streaming Java API Skeleton");
	}

	public static class WordWithCount{
		public String word;
		public long count;
		public WordWithCount(){}
		public WordWithCount(String word, long count) {
			this.word = word;
			this.count = count;
		}

		@Override
		public String toString() {
			return "WordWithCount{" +
					"word='" + word + '\'' +
					", count=" + count +
					'}';
		}
	}
}
