package com.bolingcavalry.controller;

import com.bolingcavalry.Constants;
import com.bolingcavalry.service.KafkaService;
import com.bolingcavalry.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Controller
public class KafkaController {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Autowired
	private RedisService redisService;

	@Autowired
	private KafkaService kafkaService;

	/**
	 * 加入一些公共信息，这样在tomcat集群的时候可以确定响应来自哪台机器
	 * @param model
	 */
	private void addCommon(String topic, Model model){
		if(null==model){
			return;
		}
		model.addAttribute("topic", topic);
		model.addAttribute("time", sdf.format(new Date()));
		Map<String, String> map = System.getenv();
		model.addAttribute("serviceSource", map.get("TOMCAT_SERVER_ID"));
	}

	@RequestMapping("/send")
	public String send(HttpServletRequest request, Model model) {

		String message = request.getParameter("message");

		System.out.println("send -> " + message);

		String topic = Constants.TOPIC;

		kafkaService.produce(topic, message);

		model.addAttribute("message", message);
		addCommon(topic, model);
		return "send";
	}

	@RequestMapping("/start")
	public String get(HttpServletRequest request, Model model) {
		String topic = Constants.TOPIC;
		kafkaService.startConsume(topic);
		addCommon(topic, model);
		return "start";
	}
}