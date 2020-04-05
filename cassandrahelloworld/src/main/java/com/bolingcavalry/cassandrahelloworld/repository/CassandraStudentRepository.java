package com.bolingcavalry.cassandrahelloworld.repository;

import com.bolingcavalry.cassandrahelloworld.model.Student;
import com.bolingcavalry.cassandrahelloworld.model.StudentKey;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CassandraStudentRepository implements StudentRepository {

    private final CassandraTemplate cassandraTemplate;

    public CassandraStudentRepository(CassandraTemplate cassandraTemplate) {
        this.cassandraTemplate = cassandraTemplate;
    }

        @Override
    public List<Student> findByName(String name) {
        Select select = QueryBuilder.select().from("student");
        select.where(QueryBuilder.eq("name", name));
        return this.cassandraTemplate.select(select, Student.class);
    }

    @Override
    public Student save(Student student) {
        return this.cassandraTemplate.insert(student);
    }

    @Override
    public void delete(StudentKey studentKey) {
        this.cassandraTemplate.deleteById(studentKey, Student.class);
    }
}
