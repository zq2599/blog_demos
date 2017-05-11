package com.bolingcavalry.controller;

import com.bolingcavalry.service.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


@Controller
public class KafkaController {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

		if(!StringUtils.isEmpty(topic)) {
			model.addAttribute("topic", topic);
		}

		model.addAttribute("time", sdf.format(new Date()));
		Map<String, String> map = System.getenv();
		model.addAttribute("serviceSource", map.get("TOMCAT_SERVER_ID"));
	}


	@RequestMapping("/poststart")
	public String poststart(HttpServletRequest request, Model model) {
		String topic = request.getParameter("topic");;
		kafkaService.startConsume(topic);
		addCommon(topic, model);
		return "start_finish";
	}


	//start of entry

	@RequestMapping("/start")
	public String start(HttpServletRequest request, Model model) {
		addCommon(null, model);
		return "start";
	}

	//end of entry
}