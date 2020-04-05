package com.bolingcavalry.cassandrahelloworld.configuration;

import com.bolingcavalry.cassandrahelloworld.repository.StudentRepository;
import com.bolingcavalry.cassandrahelloworld.service.StudentService;
import com.bolingcavalry.cassandrahelloworld.service.impl.StudentServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RootConfiguration {
    @Bean
    public StudentService StudentService(StudentRepository studentRepository) {
        return new StudentServiceImpl(studentRepository);
    }
}