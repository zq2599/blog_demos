package com.bolingcavalry.cassandrahelloworld.service.impl;

import com.bolingcavalry.cassandrahelloworld.model.Student;
import com.bolingcavalry.cassandrahelloworld.model.StudentKey;
import com.bolingcavalry.cassandrahelloworld.repository.StudentRepository;
import com.bolingcavalry.cassandrahelloworld.service.StudentService;
import java.util.List;

public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }


    @Override
    public Student save(Student student) {
        this.studentRepository.save(student);
        return student;
    }

    @Override
    public void delete(StudentKey studentKey) {
        this.studentRepository.delete(studentKey);
    }

    @Override
    public List<Student> findStudent(String name) {
        return this.studentRepository.findByName(name);
    }
}
