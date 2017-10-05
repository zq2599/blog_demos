package com.bolingcavalry.service;

import com.bolingcavalry.bean.Student;
import com.bolingcavalry.dto.StudentDTO;

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
     * 根据学号删除
     * @param id
     * @return
     */
    String delete(long id);

    /**
     * 根据学号查找
     * @param id
     * @return
     */
    StudentDTO find(long id);
}
