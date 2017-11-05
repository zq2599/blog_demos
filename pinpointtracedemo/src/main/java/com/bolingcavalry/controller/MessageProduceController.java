package com.bolingcavalry.controller;

import com.alibaba.fastjson.JSON;
import com.bolingcavalry.bean.SimpleMessage;
import com.bolingcavalry.bean.Student;
import com.bolingcavalry.service.MessageService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author willzhao
 * @version V1.0
 * @Description: 发送消息相关的controller
 * @email zq2599@gmail.com
 * @Date 2017/10/28 下午09:43
 */
@Controller
public class MessageProduceController {

    protected static final Logger logger = LoggerFactory.getLogger(MessageProduceController.class);

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static String TOMCAT_ID = null;

    static{
        Map<String, String> map = System.getenv();
        TOMCAT_ID = map.get("TOMCAT_SERVER_ID");
    }

    @Autowired
    MessageService messageService;


    private String tag(){
        return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date());
    }

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

    @RequestMapping("/helloworld")
    public String toIndex(HttpServletRequest request, Model model) {
        String name = request.getParameter("name");
        model.addAttribute("name", name);
        addCommon(model);
        return "hello";
    }

    @RequestMapping("/sendrequest")
    @ResponseBody
    public String simple(HttpServletRequest request, Model model) {
        String ip = get(request, "ip");
        String port = get(request, "port");
        String content = get(request, "content");

        logger.info("start simple, ip [{}], port [{}], content [{}]", ip, port, content);
        String response = messageService.sendSimpleMsg(ip, port, content);
        logger.info("end simple, ip [{}], port [{}], content [{}]", ip, port, content);

        return String.format("success [%s], ip [%s], port [%s], content [%s], response [%s]", tag(), ip, port, content, response);
    }

    @RequestMapping("/dotrace")
    @ResponseBody
    public String dotrace(HttpServletRequest request, Model model) {
        String content = get(request, "content");
        logger.info("start dotrace, content [{}]", content);
        return String.format("trace response from %s [%s]", TOMCAT_ID, tag());
    }

    @RequestMapping("/tracegson")
    @ResponseBody
    public String tracegson(HttpServletRequest request, Model model) {
        String name = get(request, "name");
        String age = get(request, "age");

        Student student = new Student();
        student.setName(name);
        student.setAge(Integer.valueOf(age));

        Gson gson = new Gson();

        String parseStr = gson.toJson(student, Student.class);

        logger.info("gson str [{}]", parseStr);

        return String.format("gson str : %s [%s]", parseStr, tag());
    }

}
