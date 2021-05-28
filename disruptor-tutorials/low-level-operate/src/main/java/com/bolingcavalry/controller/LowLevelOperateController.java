package com.bolingcavalry.controller;

import com.bolingcavalry.service.LowLevelOperateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
public class LowLevelOperateController {

	@Autowired
	@Qualifier("oneConsumer")
	LowLevelOperateService oneConsumerService;

	@Autowired
	@Qualifier("multiConsumer")
	LowLevelOperateService threeConsumerService;

	@RequestMapping(value = "/{value}", method = RequestMethod.GET)
	public String publish(@PathVariable("value") String value) {
		oneConsumerService.publish(value);
		return "success, " + LocalDateTime.now().toString();
	}
}
