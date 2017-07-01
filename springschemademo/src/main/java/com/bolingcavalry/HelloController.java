package com.bolingcavalry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
	Computer computer;

	@RequestMapping("/hello")
	@ResponseBody
	public String toIndex(HttpServletRequest request, Model model) {
		return "hello 001 [" + sdf.format(new Date()) + "], computer os [" + computer.getOs() + "], ram [" + computer.getRam() + "]";
	}
}