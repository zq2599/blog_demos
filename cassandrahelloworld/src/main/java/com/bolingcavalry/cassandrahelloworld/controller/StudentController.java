package com.bolingcavalry.cassandrahelloworld.controller;

import com.bolingcavalry.cassandrahelloworld.model.Student;
import com.bolingcavalry.cassandrahelloworld.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping(path = "/findbyname/{name}")
    public List<Student> findHotelsWithLetter(@PathVariable("name") String name) {
        return this.studentService.findStudent(name);
    }
}
