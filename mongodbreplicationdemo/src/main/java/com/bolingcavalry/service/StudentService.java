package com.bolingcavalry.service;

import com.bolingcavalry.entity.Student;

import java.util.List;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 学生服务的接口
 * @email zq2599@gmail.com
 * @Date 2017/10/5 上午9:28
 */
public interface StudentService {

    /**
     * 新增
     * @param student
     * @return
     */
    String insert(Student student);

    /**
     * 根据名字删除
     * @param name
     * @return
     */
    String deleteByName(String name);

    /**
     * 查找所有数据
     * @return
     */
    List<Student> findAll();

    /**
     * 根据名字更新信息
     * @param student
     * @return
     */
    String updateByName(Student student);
}
