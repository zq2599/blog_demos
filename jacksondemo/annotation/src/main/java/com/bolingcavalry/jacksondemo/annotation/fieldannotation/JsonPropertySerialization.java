package com.bolingcavalry.jacksondemo.annotation.fieldannotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonPropertySerialization {

    static class Test {

        @JsonProperty(value="json_field0", index = 1)
        private String field0;

        @JsonProperty(value="json_field1", index = 0)
        public String getField1() {
            return "111";
        }

        public Test(String field0) {
            this.field0 = field0;
        }
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // 美化输出
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Test test = new Test("000");

        System.out.println(mapper.writeValueAsString(test));
    }
}
