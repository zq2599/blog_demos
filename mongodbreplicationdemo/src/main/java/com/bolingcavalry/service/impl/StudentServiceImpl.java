package com.bolingcavalry.service.impl;

import com.alibaba.fastjson.JSON;
import com.bolingcavalry.entity.Student;
import com.bolingcavalry.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 学生服务类的实现
 * @email zq2599@gmail.com
 * @Date 2017/10/5 上午9:47
 */
@Service
public class StudentServiceImpl implements StudentService{

    protected static final Logger LOGGER = LoggerFactory.getLogger(StudentServiceImpl.class);

    @Autowired
    MongoTemplate mongoTemplate;

    public String insert(Student student) {
        LOGGER.info("start insert : {}", JSON.toJSONString(student));

        mongoTemplate.insert(student);

        LOGGER.info("finish insert");

        return null;
    }

    public String deleteByName(String name) {
        LOGGER.info("start delete [{}]", name);
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("name").is(name);
        query.addCriteria(criteria);
        mongoTemplate.findAndRemove(query, Student.class);
        return null;
    }

    public List<Student> findAll() {
        return mongoTemplate.findAll(Student.class);
    }

    public String updateByName(Student student) {
        LOGGER.info("start update [{}]", JSON.toJSONString(student));
        //构造查询信息
        Query query = buildNameQuery(student.getName());

        //构造更新信息
        Update update = new Update();
        update.set("age", student.getAge());

        //执行更新
        mongoTemplate.updateFirst(query, update, Student.class);

        return null;
    }

    /**
     * 创建一个根据名字查询的Query对象
     * @param name
     * @return
     */
    private Query buildNameQuery(String name){
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("name").is(name);
        query.addCriteria(criteria);

        return query;
    }
}
