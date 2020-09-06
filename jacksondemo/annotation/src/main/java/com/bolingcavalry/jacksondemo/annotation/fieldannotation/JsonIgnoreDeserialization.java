package com.bolingcavalry.jacksondemo.annotation.fieldannotation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonIgnoreDeserialization {

    static class Test {

        private String field0;

        @JsonIgnore
        private String field1;

        private String field2;

        public String getField0() { return field0; }
        public void setField0(String field0) { this.field0 = field0; }
        public String getField1() { return field1; }
        public void setField1(String field1) { this.field1 = field1; }
        public String getField2() { return field2; }

        @JsonIgnore
        public void setField2(String field2) { this.field2 = field2; }

        @Override
        public String toString() {
            return "Test{" +
                    "field0='" + field0 + '\'' +
                    ", field1='" + field1 + '\'' +
                    ", field2='" + field2 + '\'' +
                    '}';
        }
    }

    public static void main(String[] args) throws Exception {
        String jsonStr = "{\n" +
                "  \"field0\" : \"000\",\n" +
                "  \"field1\" : \"111\",\n" +
                "  \"field2\" : \"222\"\n" +
                "}";

        System.out.println(new ObjectMapper().readValue(jsonStr, Test.class));
    }
}
