package com.bolingcavalry.cassandrahelloworld.service;

import com.bolingcavalry.cassandrahelloworld.model.Student;
import com.bolingcavalry.cassandrahelloworld.model.StudentKey;

import java.util.List;

public interface StudentService {
    Student save(Student student);
    void delete(StudentKey studentKey);
    List<Student> findStudent(String name);
}
