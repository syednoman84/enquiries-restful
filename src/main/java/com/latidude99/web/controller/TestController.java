package com.latidude99.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TestController {
	
	@RequestMapping("/testError")
	public void handleRequest() {
		throw new RuntimeException("test exception");
	}

}
