package com.bolingcavalry.jacksondemo.annotation.methodannotation;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonGetterSerialization {

    static class Test {

        @JsonGetter("jsongetter_field0")
        public String getField0() { return "000"; }

    }

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Test test = new Test();
        System.out.println(mapper.writeValueAsString(test));
    }
}
