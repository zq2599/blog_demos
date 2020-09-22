package com.bolingcavalry.jacksondemo.annotation.jsoninclude;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class NonAbsent {

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
      static class Test {
        private String field0;
        private String field1;
        private Optional<String> field2;
        private AtomicReference<String> field3;

        public String getField0() { return field0; }
        public void setField0(String field0) { this.field0 = field0; }
        public String getField1() { return field1; }
        public void setField1(String field1) { this.field1 = field1; }
        public Optional<String> getField2() { return field2; }
        public void setField2(Optional<String> field2) { this.field2 = field2; }
        public AtomicReference<String> getField3() { return field3; }
        public void setField3(AtomicReference<String> field3) { this.field3 = field3; }
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // 美化输出
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // jackson支持Optional特性
        mapper.registerModule(new Jdk8Module());

        Test test = new Test();
        test.setField0(null);
        test.setField1("");
        test.setField2(Optional.empty());
        test.setField3(new AtomicReference<>());

        System.out.println(mapper.writeValueAsString(test));
    }
}
