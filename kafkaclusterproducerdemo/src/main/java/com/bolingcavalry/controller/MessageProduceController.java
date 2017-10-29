package com.bolingcavalry.controller;

import com.alibaba.fastjson.JSON;
import com.bolingcavalry.bean.SimpleMessage;
import com.bolingcavalry.service.MessageService;
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
        String numStr = get(request, "num");

        logger.info("start simple, topic [{}], content [{}]", topic, content);
        if(!StringUtils.isEmpty(numStr)){
            int num = Integer.valueOf(numStr);

            for(int i=0;i<num;i++){
                messageService.sendSimpleMsg(topic, content + "-" + i);
            }
        }else{
            messageService.sendSimpleMsg(topic, content);
        }

        logger.info("end simple, topic [{}], content [{}]", topic, content);

        return String.format("success [%s], topic [%s], content [%s]", tag(), topic, content);
    }

    @RequestMapping("/keymessage")
    @ResponseBody
    public String keymessage(HttpServletRequest request, Model model) {
        String topic = get(request, "topic");
        String content = get(request, "content");
        String keyStr = get(request, "key");

        SimpleMessage simpleMessage = new SimpleMessage();
        simpleMessage.setContent(content);
        simpleMessage.setFrom(TOMCAT_ID);

        String message = JSON.toJSONString(simpleMessage);

        logger.info("start simple, topic [{}], key [{}], message [{}]", topic, keyStr, message);
        messageService.sendKeyMsg(topic, keyStr, message);
        logger.info("end simple, topic [{}], key [{}], message [{}]", topic, keyStr, message);

        return String.format("success [%s], topic [%s], key [%s], content [%s]", tag(), topic, keyStr, content);
    }
}
