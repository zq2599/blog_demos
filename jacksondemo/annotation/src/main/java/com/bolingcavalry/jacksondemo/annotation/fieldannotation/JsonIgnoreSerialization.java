package com.bolingcavalry.jacksondemo.annotation.fieldannotation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonIgnoreSerialization {

    static class Test {

        private String field0;

        @JsonIgnore
        private String field1;

        private String field2;

        public String getField0() { return field0; }
        public void setField0(String field0) { this.field0 = field0; }
        public String getField1() { return field1; }
        public void setField1(String field1) { this.field1 = field1; }
        public void setField2(String field2) { this.field2 = field2; }

        @JsonIgnore
        public String getField2() { return field2; }
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // 美化输出
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Test test = new Test();
        test.setField0("000");
        test.setField1("111");
        test.setField2("222");

        System.out.println(mapper.writeValueAsString(test));
    }
}
