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

	private static final int MAX = 300;
	private static final int MIN = 0;

	@RequestMapping("/count")
	@ResponseBody
	public int count() {
		return 1000 + new Random().nextInt(MAX)%(MAX-MIN+1);
	}
}