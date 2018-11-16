package com.latidude99.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.latidude99.model.Enquiry;
import com.latidude99.model.User;
import com.latidude99.util.EnquiryListWrapper;

@Controller
public class HomeController {
	
	@GetMapping("/")
	public String home(Model model) {
		Enquiry enquiry = new Enquiry();
		model.addAttribute("enquiry", enquiry);
		model.addAttribute("user", new User());
		model.addAttribute("uploadFail", null);
		return "enquiryForm";
	}
		
	@GetMapping("/index")
	public String index() {
		return "redirect: /";
	}
	
	@GetMapping("/terms")
	public String terms() {
		return "terms";
	}

}












