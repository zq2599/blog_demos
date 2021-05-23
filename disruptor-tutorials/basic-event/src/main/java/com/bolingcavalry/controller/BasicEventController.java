package com.bolingcavalry.controller;

import com.bolingcavalry.service.BasicEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
public class BasicEventController {

	@Autowired
	BasicEventService basicEventService;

	@RequestMapping(value = "/{value}", method = RequestMethod.GET)
	public String publish(@PathVariable("value") String value) {
		basicEventService.publish(value);
		return "success, " + LocalDateTime.now().toString();
	}
}
