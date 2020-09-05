package com.bolingcavalry.jacksondemo.annotation.classannonation;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonRootNameDemo {

    @JsonRootName(value = "aaabbbccc")
    static class Test {
        private String field0;

        public String getField0() { return field0; }
        public void setField0(String field0) { this.field0 = field0; }
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // 美化输出
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 开启了WRAP_ROOT_VALUE的时候
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);

        Test test = new Test();
        test.setField0("123");

        System.out.println(mapper.writeValueAsString(test));
    }
}
