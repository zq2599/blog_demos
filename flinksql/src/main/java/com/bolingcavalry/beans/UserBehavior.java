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

package com.bolingcavalry.beans;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * @Description: 用户行为信息的实体类
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2020/5/2 15:02
 */
public class UserBehavior {


    @JsonFormat
    private long user_id;

    @JsonFormat
    private long item_id;

    @JsonFormat
    private long category_id;

    @JsonFormat
    private String behavior;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private Date ts;


    public UserBehavior() {
    }

    public UserBehavior(long user_id, long item_id, long category_id, String behavior, Date ts) {
        this.user_id = user_id;
        this.item_id = item_id;
        this.category_id = category_id;
        this.behavior = behavior;
        this.ts = ts;
    }
}
