/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bolingcavalry;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

public class StreamingJob {

	public static void main(String[] args) throws Exception {

		//环境信息
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		//数据来源是本机9999端口，换行符分隔，您也可以考虑将hostname和port参数通过main方法的入参传入
		DataStream<String> text = env.socketTextStream("localhost", 9999, "\n");

		//通过text对象转换得到新的DataStream对象，
		//转换逻辑是分隔每个字符串，取得的所有单词都创建一个WordWithCount对象
		DataStream<WordWithCount> windowCounts = text.flatMap(new FlatMapFunction<String, WordWithCount>() {
			@Override
			public void flatMap(String s, Collector<WordWithCount> collector) throws Exception {
				for(String word : s.split("\\s")){
					collector.collect(new WordWithCount(word, 1L));
				}
			}
		})
		.keyBy("word")//key为word字段
		.timeWindow(Time.seconds(5))	//五秒一次的翻滚时间窗口
		.reduce(new ReduceFunction<WordWithCount>() { //reduce策略
			@Override
			public WordWithCount reduce(WordWithCount a, WordWithCount b) throws Exception {
				return new WordWithCount(a.word, a.count+b.count);
			}
		});


		//单线程输出结果
		windowCounts.print().setParallelism(1);

		// 执行
		env.execute("Flink Streaming Java API Skeleton");
	}

	/**
	 * 记录单词及其出现频率的Pojo
	 */
	public static class WordWithCount {
		/**
		 * 单词内容
		 */
		public String word;

		/**
		 * 出现频率
		 */
		public long count;

		public WordWithCount() {
			super();
		}

		public WordWithCount(String word, long count) {
			this.word = word;
			this.count = count;
		}

		/**
		 * 将单词内容和频率展示出来
		 * @return
		 */
		@Override
		public String toString() {
			return word + " : " + count;
		}
	}
}
