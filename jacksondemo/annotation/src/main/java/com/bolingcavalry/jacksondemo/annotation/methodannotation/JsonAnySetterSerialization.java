package com.bolingcavalry.jacksondemo.annotation.methodannotation;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.HashMap;
import java.util.Map;

public class JsonAnySetterSerialization {

    static class Test {
        private String field0;
        private Map<String, Object> map;

        public String getField0() { return field0; }
        public void setField0(String field0) { this.field0 = field0; }
        public void setMap(Map<String, Object> map) { this.map = map; }

        @JsonAnyGetter
        public Map<String, Object> getMap() { return map; }
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // 美化输出
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // 新增一个HashMap，里面放入两个元素
        Map<String, Object> map = new HashMap<>();
        map.put("aaa", "value_aaa");
        map.put("bbb", "value_bbb");

        Test test = new Test();
        test.setField0("000");

        // map赋值给test.map
        test.setMap(map);

        System.out.println(mapper.writeValueAsString(test));
    }
}
