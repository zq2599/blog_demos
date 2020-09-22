package com.bolingcavalry.jacksondemo.annotation.jsoninclude;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class UseDefaults {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    static class Test {

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private List<String> field0;

        @JsonInclude(JsonInclude.Include.USE_DEFAULTS)
        public List<String> getField0() { return field0; }

        public void setField0(List<String> field0) { this.field0 = field0; }
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Test test = new Test();

        test.setField0(new ArrayList<>());

        System.out.println(mapper.writeValueAsString(test));
    }
}
