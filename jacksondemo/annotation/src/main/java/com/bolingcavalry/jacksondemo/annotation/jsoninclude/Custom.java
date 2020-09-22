package com.bolingcavalry.jacksondemo.annotation.jsoninclude;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Custom {

    static class CustomFilter {
        @Override
        public boolean equals(Object obj) {
            // null，或者不是字符串就返回true，意味着不被序列化
            if(null==obj || !(obj instanceof String)) {
                return true;
            }

            // 长度大于2就返回true，意味着不被序列化
            return ((String) obj).length() > 2;
        }
    }

    static class Test {

        @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = CustomFilter.class)
        private String field0;

        @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = CustomFilter.class)
        private String field1;

        @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = CustomFilter.class)
        private String field2;

        public String getField0() { return field0; }
        public void setField0(String field0) { this.field0 = field0; }
        public String getField1() { return field1; }
        public void setField1(String field1) { this.field1 = field1; }
        public String getField2() { return field2; }
        public void setField2(String field2) { this.field2 = field2; }
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // 美化输出
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Test test = new Test();
        test.setField0(null);
        test.setField1("1");
        test.setField2("123");

        System.out.println(mapper.writeValueAsString(test));
    }
}
