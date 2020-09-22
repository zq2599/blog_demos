package com.bolingcavalry.jacksondemo.annotation.methodannotation;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class JsonAnySetterDeserialization {

    static class Test {

        private String field0;

        private Map<String, Object> map = new HashMap<>();

        @JsonAnySetter
        public void setValue(String key, Object value) {
            map.put(key, value);
        }

        @Override
        public String toString() {
            return "Test{" +
                    "field0='" + field0 + '\'' +
                    ", map=" + map +
                    '}';
        }
    }

    public static void main(String[] args) throws Exception {
        String jsonStr = "{\n" +
                "  \"field0\" : \"000\",\n" +
                "  \"aaa\" : \"value_aaa\",\n" +
                "  \"bbb\" : \"value_bbb\"\n" +
                "}";

        System.out.println(new ObjectMapper().readValue(jsonStr, Test.class));
    }
}
