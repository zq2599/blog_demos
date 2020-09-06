package com.bolingcavalry.jacksondemo.annotation.fieldannotation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.util.Date;

public class JsonDeserializeDeserialization {



    static class Long2DateDeserialize extends JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

            if(null!=p && null!=ctxt && p.getLongValue()>0L ) {
                return new Date(p.getLongValue());
            }

            return null;
        }
    }

    static class Test {

        @JsonDeserialize(using = Long2DateDeserialize.class)
        private Date field0;

        @Override
        public String toString() { return "Test{" + "field0='" + field0 + '\'' + '}'; }
    }

    public static void main(String[] args) throws Exception {
        String jsonStr = "{\"field0\" : 1599371554563}";

        System.out.println(new ObjectMapper().readValue(jsonStr, Test.class));
    }
}
