package com.bolingcavalry.jacksondemo.annotation.fieldannotation;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonRawValueSerialization {

    static class Test {

        @JsonRawValue
        private String field0 = "abc";
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new ObjectMapper().writeValueAsString(new Test()));
    }
}
