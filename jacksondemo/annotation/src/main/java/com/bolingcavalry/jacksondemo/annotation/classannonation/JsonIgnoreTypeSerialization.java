package com.bolingcavalry.jacksondemo.annotation.classannonation;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonIgnoreTypeSerialization {

    @JsonIgnoreType
    static class TestChild {
        private int value;

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "TestChild{" + "value=" + value + '}';
        }
    }


    static class Test {
        private String field0;
        private TestChild field1;

        public String getField0() {
            return field0;
        }

        public void setField0(String field0) {
            this.field0 = field0;
        }

        public TestChild getField1() {
            return field1;
        }

        public void setField1(TestChild field1) {
            this.field1 = field1;
        }

        @Override
        public String toString() {
            return "Test{" +
                    "field0='" + field0 + '\'' +
                    ", field1=" + field1 +
                    '}';
        }
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // 美化输出
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        TestChild testChild = new TestChild();
        testChild.setValue(123);

        Test test = new Test();
        test.setField0("aaa");
        test.setField1(testChild);

        System.out.println(mapper.writeValueAsString(test));
    }
}
