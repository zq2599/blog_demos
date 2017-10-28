package com.bolingcavalry.dto;

import com.bolingcavalry.bean.Student;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 用一句话描述该文件做什么
 * @email zq2599@gmail.com
 * @Date 2017/10/5 上午9:31
 */
public class StudentDTO {
    /**
     * 返回的错误信息
     */
    String errorStr;

    /**
     * 查找结果
     */
    Student student;

    public String getErrorStr() {
        return errorStr;
    }

    public void setErrorStr(String errorStr) {
        this.errorStr = errorStr;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }
}
