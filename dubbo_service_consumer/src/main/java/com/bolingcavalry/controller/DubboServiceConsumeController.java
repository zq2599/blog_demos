package com.bolingcavalry.controller;

import com.bolingcavalry.service.CalculateService;
import com.bolingcavalry.service.PlatformService;
import com.bolingcavalry.util.Tools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


@Controller
public class DubboServiceConsumeController {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Autowired
	private CalculateService calculateService;

	@Autowired
	private PlatformService platformService;

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

	@RequestMapping("/postadd")
	public String postadd(HttpServletRequest request, Model model) {

		int param0 = Tools.getInt(request, "param0", 0);
		int param1 = Tools.getInt(request, "param1", 0);

		model.addAttribute("calcRlt", String.valueOf(calculateService.add(param0, param1)));
		model.addAttribute("rpcFrom", platformService.getRpcFrom());
		addCommon(model);
		return "add_finish";
	}

	//start of entry

	@RequestMapping("/add")
	public String send(HttpServletRequest request, Model model) {
		addCommon(model);
		return "add";
	}

	//end of entry
}