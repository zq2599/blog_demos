package com.bolingcavalry.cassandrahelloworld.model;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("student")
public class Student {

    public Student() {}

    @PrimaryKey
    private StudentKey studentKey;

    private Integer age;

    public StudentKey getStudentKey() {
        return studentKey;
    }

    public void setStudentKey(StudentKey studentKey) {
        this.studentKey = studentKey;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
