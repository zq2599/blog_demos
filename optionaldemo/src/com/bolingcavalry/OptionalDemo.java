package com.bolingcavalry;

import javax.swing.text.html.Option;
import java.util.Optional;

/**
 * @author willzhao
 * @version V1.0
 * @Description: Optional用法示例
 * @email zq2599@gmail.com
 * @Date 2017/8/26 下午11:18
 */
public class OptionalDemo {


    private static final Student DEFAULT = new Student(0, "default", 0);


    private Student queryById(int id){
        //TODO 这里模拟从数据库查询
        return null;
    }


    private Student getDefault(){
        return DEFAULT;
    }

    public Student getStudent(int id){
        Optional<Student> optional = Optional.ofNullable(queryById(id));

        //如果为空就返回DEFAULT对象
        return optional.orElseGet(() -> getDefault());
    }

    public String getStudentUpperName(int id){
        Optional<Student> optional = Optional.ofNullable(queryById(id));

        return optional.map(student -> student.getName())
                .map(name -> name.toUpperCase())
                .orElse("invalid");
    }



    public static void main(String[] args){
        OptionalDemo optionalDemo = new OptionalDemo();

        System.out.println(optionalDemo.getStudent(1).getName());





    }
}
