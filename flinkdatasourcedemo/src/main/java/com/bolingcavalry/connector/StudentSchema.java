package com.bolingcavalry.connector;

import com.bolingcavalry.Student;
import com.google.gson.Gson;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

/**
 * @author will
 * @email zq2599@gmail.com
 * @date 2020-03-15 23:44
 * @description 功能介绍
 */
public class StudentSchema implements DeserializationSchema<Student>, SerializationSchema<Student> {

    private static final Gson gson = new Gson();

    /**
     * 反序列化，将byte数组转成Student实例
     * @param bytes
     * @return
     * @throws IOException
     */
    @Override
    public Student deserialize(byte[] bytes) throws IOException {
        return gson.fromJson(new String(bytes), Student.class);
    }

    @Override
    public boolean isEndOfStream(Student student) {
        return false;
    }

    /**
     * 序列化，将Student实例转成byte数组
     * @param student
     * @return
     */
    @Override
    public byte[] serialize(Student student) {
        return new byte[0];
    }

    @Override
    public TypeInformation<Student> getProducedType() {
        return TypeInformation.of(Student.class);
    }
}
