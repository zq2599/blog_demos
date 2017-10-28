package com.bolingcavalry.controller;

import com.bolingcavalry.service.MessageService;
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
 * @Description: 发送消息相关的controller
 * @email zq2599@gmail.com
 * @Date 2017/10/28 下午09:43
 */
@Controller
public class MessageProduceController {

    protected static final Logger logger = LoggerFactory.getLogger(MessageProduceController.class);

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

    @RequestMapping("/hello")
    public String toIndex(HttpServletRequest request, Model model) {
        String name = request.getParameter("name");
        model.addAttribute("name", name);
        addCommon(model);
        return "hello";
    }

    @RequestMapping("/simple")
    @ResponseBody
    public String simple(HttpServletRequest request, Model model) {
        String topic = get(request, "topic");
        String content = get(request, "content");

        logger.info("start simple, topic [{}], content [{}]");

        logger.info("end simple, topic [{}], content [{}]");

        return String.format("success [%s], topic [%s], content [%s]", tag(), topic, content);
    }
}
