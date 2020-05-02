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
import com.csvreader.CsvReader;

import java.io.IOException;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * @Description: 读取数据
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/5/2 15:02
 */
public class UserBehaviorCsvFileReader implements Supplier<UserBehavior> {

    private final String filePath;
    private CsvReader csvReader;

    public UserBehaviorCsvFileReader(String filePath) throws IOException {

        this.filePath = filePath;
        try {
            csvReader = new CsvReader(filePath);
            csvReader.readHeaders();
        } catch (IOException e) {
            throw new IOException("Error reading TaxiRecords from file: " + filePath, e);
        }
    }

    @Override
    public UserBehavior get() {
        UserBehavior userBehavior = null;
        try{
            if(csvReader.readRecord()) {
                csvReader.getRawRecord();
                userBehavior = new UserBehavior(
                        Long.valueOf(csvReader.get(0)),
                        Long.valueOf(csvReader.get(1)),
                        Long.valueOf(csvReader.get(2)),
                        csvReader.get(3),
                        new Date(Long.valueOf(csvReader.get(4))*1000L));
            }
        } catch (IOException e) {
            throw new NoSuchElementException("IOException from " + filePath);
        }

        if (null==userBehavior) {
            throw new NoSuchElementException("All records read from " + filePath);
        }

        return userBehavior;
    }
}
