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

import com.bolingcavalry.beans.UserBehavior;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Produces TaxiRecords (Ride, Fare, DriverChange) into Kafka topics.
 */
public class SendMessageApplication {

    public static void main(String[] args) throws Exception {
        Supplier<UserBehavior> userBehaviorSupplier = new UserBehaviorCsvFileReader("D:\\temp\\202005\\02\\UserBehavior1.csv");

        Consumer<UserBehavior> userBehaviorConsumer = new KafkaProducer("user_behavior2", "192.168.50.43:9092");;

        // create three threads for each record type
        Thread userBehaviorFeeder = new Thread(new RecordFeeder(userBehaviorSupplier, userBehaviorConsumer));

        // start emitting data
        userBehaviorFeeder.start();

        // wait for threads to complete
        userBehaviorFeeder.join();
    }

    public static class RecordFeeder implements Runnable {

        private final Supplier<UserBehavior> source;
        private final Consumer<UserBehavior> sink;

        RecordFeeder(Supplier<UserBehavior> source, Consumer<UserBehavior> sink) {
            this.source = source;
            this.sink = sink;
        }

        @Override
        public void run() {
            Stream.generate(source).sequential()
                    .forEachOrdered(sink);
        }
    }
}
