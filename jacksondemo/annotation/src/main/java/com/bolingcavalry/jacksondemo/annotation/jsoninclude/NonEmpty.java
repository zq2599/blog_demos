package com.bolingcavalry.jacksondemo.annotation.jsoninclude;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class NonEmpty {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
      static class Test {
        private String field0;
        private String field1;
        private Optional<String> field2;
        private AtomicReference<String> field3;
        private List<String> field4;
        private String[] field5;

        public String getField0() { return field0; }
        public void setField0(String field0) { this.field0 = field0; }
        public String getField1() { return field1; }
        public void setField1(String field1) { this.field1 = field1; }
        public Optional<String> getField2() { return field2; }
        public void setField2(Optional<String> field2) { this.field2 = field2; }
        public AtomicReference<String> getField3() { return field3; }
        public void setField3(AtomicReference<String> field3) { this.field3 = field3; }
        public List<String> getField4() { return field4; }
        public void setField4(List<String> field4) { this.field4 = field4; }
        public String[] getField5() { return field5; }
        public void setField5(String[] field5) { this.field5 = field5; }
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
        test.setField4(new ArrayList<>());
        test.setField5(new String[] {});

        System.out.println(mapper.writeValueAsString(test));
    }
}
