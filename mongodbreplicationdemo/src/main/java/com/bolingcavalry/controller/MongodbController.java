package com.bolingcavalry.controller;

import com.alibaba.fastjson.JSON;
import com.bolingcavalry.entity.Student;
import com.bolingcavalry.service.StudentService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 提供mongodb服务的页面controller
 * @email zq2599@gmail.com
 * @Date 2017/10/4 上午8:45
 */
@Controller
public class MongodbController {

    protected static final Logger LOGGER = LoggerFactory.getLogger(MongodbController.class);

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    StudentService studentService;

    /**
     * 加入一些公共信息，这样在tomcat集群的时候可以确定响应来自哪台机器
     * @param model
     */
    private void addCommon(Model model){
        if(null==model){
            return;
        }
        model.addAttribute("time", sdf.format(new Date()));
    }

    private String buileFilePageInfo(String errorStr, Model model){
        addCommon(model);
        model.addAttribute("errorStr", errorStr);
        return "fail";
    }

    /**
     * 从HttpServletRequest中获取执行参数
     * @param request
     * @param name
     * @return
     */
    private static String get(HttpServletRequest request, String name){
        return request.getParameter(name);
    }

    /**
     * 从HttpServletRequest中获取参数，构造一个Student对象
     * @param request
     * @return
     */
    private static Student buildStudent(HttpServletRequest request){
        String name = get(request, "name");
        String age = get(request, "age");

        Student student = new Student();
        student.setName(name);
        student.setAge(Integer.valueOf(age));

        return student;
    }

    @RequestMapping("/hello")
    public String toIndex(HttpServletRequest request, Model model) {
        String name = request.getParameter("name");
        model.addAttribute("name", name);
        addCommon(model);
        return "hello";
    }

    @RequestMapping("/insert")
    public String insert(HttpServletRequest request, Model model) {
        String errorStr = studentService.insert(buildStudent(request));

        LOGGER.info("student insert service response [{}]", errorStr);

        return StringUtils.isEmpty(errorStr) ? "success" : buileFilePageInfo(errorStr, model);
    }


    @RequestMapping("/delete")
    public String delete(HttpServletRequest request, Model model) {
        String name = get(request, "name");

        String errorStr = studentService.deleteByName(name);

        LOGGER.info("student deleteByName service response [{}]", errorStr);

        return StringUtils.isEmpty(errorStr) ? "success" : buileFilePageInfo(errorStr, model);
    }

    @RequestMapping("/update")
    public String update(HttpServletRequest request, Model model) {
        String errorStr = studentService.updateByName(buildStudent(request));
        LOGGER.info("student updateByName service response [{}]", errorStr);
        return StringUtils.isEmpty(errorStr) ? "success" : buileFilePageInfo(errorStr, model);
    }



    @RequestMapping("/findall")
    @ResponseBody
    public String findAll(HttpServletRequest request, Model model) {
        List<Student> list = studentService.findAll();

        return (null!=list && !list.isEmpty())
                ? JSON.toJSONString(list)
                : "empty";
    }


}
