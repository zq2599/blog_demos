package com.bolingcavalry.jacksondemo.annotation.fieldannotation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.util.Date;

public class JsonSerializeSerialization {

    static class Date2LongSerialize extends JsonSerializer<Date> {

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeNumber(value.getTime());
        }
    }

    static class Test {
        @JsonSerialize(using = Date2LongSerialize.class)
        private Date field0 = new Date();
    }

    public static void main(String[] args) throws Exception {
        Test test = new Test();
        System.out.println(new ObjectMapper().writeValueAsString(test));
    }
}
