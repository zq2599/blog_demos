package com.bolingcavalry.jacksondemo.annotation.classannonation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonIgnorePropertiesDeserializer {

    @JsonIgnoreProperties({"field1", "field2"})
    static class Test {
        private String field0;
        private String field1;
        private String field2;

        public String getField0() {
            return field0;
        }

        public void setField0(String field0) {
            this.field0 = field0;
        }

        public String getField1() {
            return field1;
        }

        public void setField1(String field1) {
            this.field1 = field1;
        }

        public String getField2() {
            return field2;
        }

        public void setField2(String field2) {
            this.field2 = field2;
        }

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
                "  \"field0\" : \"111\",\n" +
                "  \"field1\" : \"222\",\n" +
                "  \"field2\" : \"333\"\n" +
                "}";

        System.out.println(new ObjectMapper().readValue(jsonStr, Test.class));
    }
}
