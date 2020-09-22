package com.bolingcavalry.jacksondemo.annotation.methodannotation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonCreatorDeserialization {

    static class Test {

        private String field0;
        private String field1;


        public Test(String field0) {
            this.field0 = field0;
        }

        // 通过JsonCreator指定反序列化的时候使用这个构造方法
        // 通过JsonProperty指定字段关系
        @JsonCreator
        public Test(@JsonProperty("field0") String field0,
                    @JsonProperty("field1") String field1) {
            this.field0 = field0;
            this.field1 = field1;
        }

        @Override
        public String toString() {
            return "Test{" +
                    "field0='" + field0 + '\'' +
                    ", field1='" + field1 + '\'' +
                    '}';
        }
    }

    public static void main(String[] args) throws Exception {
        String jsonStr = "{\"field0\" : \"111\", \"field1\" : \"222\"}";

        System.out.println(new ObjectMapper().readValue(jsonStr, Test.class));
    }
}
