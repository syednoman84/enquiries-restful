package com.latidude99.web.controller;

import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.WebRequest;

@Controller
public class HomeController {
	
	@GetMapping("/")
	public String home(WebRequest request) {
		return "enquiryForm";
	}
	
	@GetMapping("/index")
	public String index() {
		return "enquiryForm";
	}
	
	@GetMapping("/terms")
	public String terms() {
		return "terms";
	}

}













