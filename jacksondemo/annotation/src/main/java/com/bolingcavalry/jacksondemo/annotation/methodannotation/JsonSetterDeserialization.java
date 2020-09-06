package com.bolingcavalry.jacksondemo.annotation.methodannotation;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSetterDeserialization {

    static class Test {

        private String field0;

        @JsonSetter("abc_field0")
        public void setField0(String field0) { this.field0 = field0; }

        @Override
        public String toString() { return "Test{" + "field0='" + field0 + '\'' + '}'; }
    }

    public static void main(String[] args) throws Exception {
        String jsonStr = "{\"abc_field0\" : \"111\"}";
        System.out.println(new ObjectMapper().readValue(jsonStr, Test.class));
    }
}
