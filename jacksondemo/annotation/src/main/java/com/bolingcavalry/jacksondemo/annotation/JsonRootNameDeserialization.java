package com.bolingcavalry.jacksondemo.annotation;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonRootNameDeserialization {

    public static void main(String[] args) throws Exception {
//        String jsonStr = "{\n" +
//                "  \"111222333\" : {\n" +
//                "    \"id\" : 2,\n" +
//                "    \"name\" : \"food\"\n" +
//                "  }\n" +
//                "}";

        String jsonStr = "{\n" +
                "    \"id\" : 2,\n" +
                "    \"name\" : \"food\"\n" +
                "  }";
        ObjectMapper mapper = new ObjectMapper();
        //mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);

        System.out.println(mapper.readValue(jsonStr, Order2.class));
    }
}