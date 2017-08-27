package com.bolingcavalry.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

@Controller
public class CountController {


	@RequestMapping("/num")
	@ResponseBody
	public int num(String model) {
		if("a".equals(model)) {
			return 666;
		}else{
			return 333;
		}
	}


	@RequestMapping("/count")
	@ResponseBody
	public String count(String model, String type) {
		int base;
		int max;
		int min;

		if("a".equals(model)){
			base = 50000;
		}else{
			base =10000;
		}

		if("0".equals(type)){
			max = 9000;
			min = 1000;
		}else{
			max = 1000;
			min = 0;
		}

		return String.valueOf(base + new Random().nextInt(max)%(max-min+1));
	}
}