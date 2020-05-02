/*
 * Copyright 2019 Ververica GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bolingcavalry.producekafkamessage;

import java.util.stream.Stream;

/**
 * @Description: 应用类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/5/2 15:02
 */
public class SendMessageApplication {

    public static void main(String[] args) throws Exception {
        // 文件地址
        String filePath = "D:\\temp\\202005\\02\\UserBehavior.csv";
        // kafka topic
        String topic = "user_behavior";
        // kafka borker地址
        String broker = "192.168.50.43:9092";


        Stream.generate(new UserBehaviorCsvFileReader(filePath))
                .sequential()
                .forEachOrdered(new KafkaProducer(topic, broker));
    }
}
