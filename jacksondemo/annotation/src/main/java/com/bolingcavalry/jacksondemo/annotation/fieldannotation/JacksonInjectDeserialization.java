package com.bolingcavalry.jacksondemo.annotation.fieldannotation;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.OptBoolean;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonInjectDeserialization {

    static class Test {

        private String field0;

        @JacksonInject(value = "defaultField1")
        private String field1;

        @JacksonInject
        private String field2;

        public String getField2() {
            return field2;
        }

        public void setField2(String field2) {
            this.field2 = field2;
        }

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
        // json字符串只有field0
        String jsonStr = "{\"field0\" : \"000\"}";

        InjectableValues.Std injectableValues = new InjectableValues.Std();

        // 指定key为"defaultField1"对应的注入参数
        injectableValues.addValue("defaultField1","field1 default value");

        // 指定String类型对应的注入参数
        injectableValues.addValue(String.class,"String type default value");

        ObjectMapper mapper = new ObjectMapper();

        // 把注入参数的配置设置给mapper
        mapper.setInjectableValues(injectableValues);

        System.out.println(mapper.readValue(jsonStr, Test.class));
    }
}
