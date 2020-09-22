package com.bolingcavalry.cassandrahelloworld.repository;

import com.bolingcavalry.cassandrahelloworld.model.Student;
import com.bolingcavalry.cassandrahelloworld.model.StudentKey;

import java.util.List;

public interface StudentRepository {
    List<Student> findByName(String name);
    Student save(Student student);
    void delete(StudentKey studentKey);
}
