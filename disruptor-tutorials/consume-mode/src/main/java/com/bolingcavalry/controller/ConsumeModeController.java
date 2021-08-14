package com.bolingcavalry.controller;

import com.bolingcavalry.service.ConsumeModeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
public class ConsumeModeController {

	@Autowired
	@Qualifier("independentModeService")
	ConsumeModeService oneConsumerService;


	@RequestMapping(value = "/{value}", method = RequestMethod.GET)
	public String publish(@PathVariable("value") String value) {
		oneConsumerService.publish(value);
		return "success, " + LocalDateTime.now().toString();
	}
}
