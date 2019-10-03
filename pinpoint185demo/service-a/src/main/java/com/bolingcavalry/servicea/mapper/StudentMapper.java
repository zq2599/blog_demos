package com.bolingcavalry.servicea.mapper;

        import com.bolingcavalry.servicea.entity.Student;
        import org.springframework.stereotype.Repository;

/**
 * @Description: mybatis mapper
 * @author: willzhao E-mail: zq2599@gmail.com
 * @date: 2019/10/3 9:58
 */
@Repository
public interface StudentMapper {

    Student getById(Integer id);

    void insert(Student student);
}
