package com.bolingcavalry.controller;

import com.alibaba.fastjson.JSON;
import com.bolingcavalry.bean.Student;
import com.bolingcavalry.dto.StudentDTO;
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

/**
 * @author willzhao
 * @version V1.0
 * @Description: HBase相关的服务
 * @email zq2599@gmail.com
 * @Date 2017/10/4 上午8:45
 */
@Controller
public class HBaseController {

    protected static final Logger LOGGER = LoggerFactory.getLogger(HBaseController.class);

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

    private String get(HttpServletRequest request, String name){
        return request.getParameter(name);
    }

    private String buileFilePageInfo(String errorStr, Model model){
        addCommon(model);
        model.addAttribute("errorStr", errorStr);
        return "fail";
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
        String id = get(request, "id");
        String name = get(request, "name");
        String age = get(request, "age");

        Student student = new Student();
        student.setId(Long.valueOf(id));
        student.setName(name);
        student.setAge(Integer.valueOf(age));

        String errorStr = studentService.insert(student);

        LOGGER.info("student service response [{}]", errorStr);

        return StringUtils.isEmpty(errorStr) ? "success" : buileFilePageInfo(errorStr, model);
    }


    @RequestMapping("/delete")
    public String delete(HttpServletRequest request, Model model) {
        String id = get(request, "id");

        String errorStr = studentService.delete(Long.valueOf(id));

        LOGGER.info("student service response [{}]", errorStr);

        return StringUtils.isEmpty(errorStr) ? "success" : buileFilePageInfo(errorStr, model);
    }

    @RequestMapping("/find")
    @ResponseBody
    public String find(HttpServletRequest request, Model model) {
        String id = get(request, "id");

        StudentDTO studentDTO = studentService.find(Long.valueOf(id));

        LOGGER.info("find result : {}", JSON.toJSONString(studentDTO));

        return StringUtils.isEmpty(studentDTO.getErrorStr())
                ? JSON.toJSONString(studentDTO.getStudent())
                : studentDTO.getErrorStr();
    }
}
