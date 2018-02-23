package com.bolingcavalry.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Controller
public class HelloController {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@RequestMapping("/hello")
	public String toIndex(HttpServletRequest request, Model model) {
		String name = request.getParameter("name");
		model.addAttribute("name", name);
		model.addAttribute("time", sdf.format(new Date()));
		Map<String, String> map = System.getenv();
		model.addAttribute("serviceSource", map.get("TOMCAT_SERVER_ID"));
		return "hello";
	}
}