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

	@RequestMapping("/strset")
	public String set(HttpServletRequest request, Model model) {
		String key = request.getParameter("key");
		String value = request.getParameter("value");
		redisService.strSet(key, value);
		model.addAttribute("key", key);
		model.addAttribute("value", value);
		addCommon(model);
		return "set_success";
	}

	@RequestMapping("/append_to_list")
	public String append_to_list(HttpServletRequest request, Model model) {
		String key = request.getParameter("key");
		String value = request.getParameter("value");

		redisService.listAppend(key, value);

		model.addAttribute("key", key);
		model.addAttribute("value", value);

		addCommon(model);

		return "listappend_success";
	}

	@RequestMapping("/get_list_by_key")
	public String get_list_by_key(HttpServletRequest request, Model model) {
		String key = request.getParameter("key");


		String rlt;

		//判断key在redis中是否存在
		if(redisService.exists(key)){
			rlt = "list_get_all_success";

			List<String> list = redisService.listGetAll(key);

			if(!list.isEmpty()) {
				model.addAttribute("value", list.get(0));
			}
			model.addAttribute("list", list);
		}else{
			rlt = "check";

			model.addAttribute("exists", "不存在");
		}


		model.addAttribute("key", key);
		addCommon(model);

		return rlt;
	}

	@RequestMapping("/strget")
	public String get(HttpServletRequest request, Model model) {
		String key = request.getParameter("key");

		String rlt;

		//判断key在redis中是否存在
		if(redisService.exists(key)){
			rlt = "simple_get_success";
			String value = redisService.setGet(key);

			model.addAttribute("value", value);
		}else{
			rlt = "check";
			model.addAttribute("exists", "不存在");
		}

		model.addAttribute("key", key);

		addCommon(model);
		return rlt;
	}

	//start of entry

	@RequestMapping("/simpleset")
	public String simpleset(HttpServletRequest request, Model model) {
		addCommon(model);
		return "simple_set";
	}

	@RequestMapping("/simpleget")
	public String simpleget(HttpServletRequest request, Model model) {
		addCommon(model);
		return "simple_get";
	}

	@RequestMapping("/listappend")
	public String listappend(HttpServletRequest request, Model model) {
		addCommon(model);
		return "list_append";
	}

	@RequestMapping("/listgetall")
	public String listgetall(HttpServletRequest request, Model model) {
		addCommon(model);
		return "list_get_all";
	}

	//end of entry
}