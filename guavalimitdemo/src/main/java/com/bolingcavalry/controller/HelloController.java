package com.bolingcavalry.controller;

import com.bolingcavalry.service.AccessLimitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Controller
public class HelloController {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Autowired
	private AccessLimitService accessLimitService;

	@RequestMapping("/hello")
	public String toIndex(HttpServletRequest request, Model model) {
		String name = request.getParameter("name");
		model.addAttribute("name", name);
		model.addAttribute("time", sdf.format(new Date()));
		Map<String, String> map = System.getenv();
		model.addAttribute("serviceSource", map.get("TOMCAT_SERVER_ID"));
		return "hello";
	}

	@RequestMapping("/access")
	@ResponseBody
	public String access(){
		//尝试获取令牌
		if(accessLimitService.tryAcquire()){
			//模拟业务执行500毫秒
			try {
				Thread.sleep(500);
			}catch (InterruptedException e){
				e.printStackTrace();
			}
			return "aceess success [" + sdf.format(new Date()) + "]";
		}else{
			return "aceess limit [" + sdf.format(new Date()) + "]";
		}
	}
}