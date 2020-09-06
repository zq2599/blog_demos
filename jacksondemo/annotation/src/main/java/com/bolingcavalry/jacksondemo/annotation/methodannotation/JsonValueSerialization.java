package com.bolingcavalry.jacksondemo.annotation.methodannotation;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonValueSerialization {

    static class Test {

        private String field0;

        private String field1;

        @JsonValue
        public String getField0() { return "abc"; }

        public void setField0(String field0) { this.field0 = field0; }
        public String getField1() { return field1; }
        public void setField1(String field1) { this.field1 = field1; }
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // 美化输出
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Test test = new Test();
        test.setField0("000");
        test.setField1("111");

        System.out.println(mapper.writeValueAsString(test));
    }
}
