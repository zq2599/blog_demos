package com.bolingcavalry.jacksondemo.annotation.jsoninclude;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class NonDefault {

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
      static class Test {

        private String field0 = "aaa";
        private String field1 = "aaa";

        public String getField0() { return field0; }
        public void setField0(String field0) { this.field0 = field0; }
        public String getField1() { return field1; }
        public void setField1(String field1) { this.field1 = field1; }
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // 美化输出
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Test test = new Test();
        test.setField1("bbb");

        System.out.println(mapper.writeValueAsString(test));
    }
}
