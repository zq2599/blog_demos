package com.bolingcavalry.controller;

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
public class RedisController {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Autowired
	private RedisService redisService;

	/**
	 * 加入一些公共信息，这样在tomcat集群的时候可以确定响应来自哪台机器
	 * @param model
	 */
	private void addCommon(Model model){
		if(null==model){
			return;
		}
		model.addAttribute("time", sdf.format(new Date()));
		Map<String, String> map = System.getenv();
		model.addAttribute("serviceSource", map.get("TOMCAT_SERVER_ID"));
	}

	@RequestMapping("/strSet")
	public String set(HttpServletRequest request, Model model) {
		String key = request.getParameter("key");
		String value = request.getParameter("value");
		redisService.strSet(key, value);
		model.addAttribute("key", key);
		model.addAttribute("value", value);
		addCommon(model);
		return "strSet";
	}

	@RequestMapping("/setGet")
	public String get(HttpServletRequest request, Model model) {
		String key = request.getParameter("key");
		String value = redisService.setGet(key);
		model.addAttribute("key", key);
		model.addAttribute("value", value);
		addCommon(model);
		return "setGet";
	}

	@RequestMapping("/check")
	public String check(HttpServletRequest request, Model model) {
		String key = request.getParameter("key");
		model.addAttribute("key", key);
		model.addAttribute("exists", redisService.exists(key) ? "存在" : "不存在");
		addCommon(model);
		return "check";
	}

	@RequestMapping("/listappend")
	public String listappend(HttpServletRequest request, Model model) {
		String key = request.getParameter("key");
		String value = request.getParameter("value");

		redisService.listAppend(key, value);

		model.addAttribute("key", key);
		model.addAttribute("value", value);
		addCommon(model);

		return "listappend";
	}

	@RequestMapping("/listgetall")
	public String listgetall(HttpServletRequest request, Model model) {
		String key = request.getParameter("key");

		List<String> list = redisService.listGetAll(key);

		model.addAttribute("key", key);

		if(!list.isEmpty()) {
			model.addAttribute("value", list.get(0));
		}
		model.addAttribute("list", list);
		addCommon(model);

		return "listgetall";
	}
}