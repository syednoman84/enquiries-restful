package com.latidude99.web.controller;

import java.security.Principal;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.latidude99.model.Enquiry;
import com.latidude99.model.User;
import com.latidude99.service.EmailService;
import com.latidude99.service.EnquiryService;
import com.latidude99.service.UserService;

@Controller
public class UserController {
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	private static final String APP_URL = "enquiry.latidude99.com";
	
	@Autowired
	private UserService userService;
	
	@Autowired
	EnquiryService enquiryService;
	
	@Autowired
	private EmailService emailService;
		
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@GetMapping("/user")
	public String user(Model model, Principal principal) {
		User currentUser = userService.getUserByUsername(principal.getName());
		model.addAttribute("currentUser", currentUser);
		Long waiting = enquiryService.getNumByStatus("waiting");
		model.addAttribute("waiting", waiting);
		Long opened = enquiryService.getNumByStatus("in progress");
		model.addAttribute("opened", opened);
		Long closed = enquiryService.getNumByStatus("closed");
		model.addAttribute("closed", closed);
		Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "opened");
		model.addAttribute("openedByUser", openedByUser);
		Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "opened");
		model.addAttribute("closedByUser", closedByUser);
		return "enquiryUser";
	}
	
	@PostMapping("/user/update")
	public String updateDetails(@ModelAttribute ("currentUser") @Valid User user, BindingResult result, Model model, Principal principal) {
		if (result.hasErrors()) {
			String currentUserName = principal.getName();
			User currentUser = userService.getUserByUsername(currentUserName);
			model.addAttribute("currentUser", currentUser);
			Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "opened");
			model.addAttribute("openedByUser", openedByUser);
			Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
			model.addAttribute("closedByUser", closedByUser);
			logger.info("password validation errors");
			result.getAllErrors().forEach(e -> System.out.println(e.toString()));
			return "enquiryUser";
		}else {
			String currentUserName = principal.getName();
			User currentUser = userService.getUserByUsername(currentUserName);
//			user.setId(currentUser.getId());
//			user.setRegistered(currentUser.getRegistered());
//			user.setRoles(currentUser.getRoles());
			if (passwordEncoder.matches(user.getPassword(), currentUser.getPassword())) {
				currentUser.setPassword(passwordEncoder.encode(user.getPasswordNew()));
//				user.setEnabled(true);
				userService.save(currentUser);
				model.addAttribute("currentUser", currentUser);
				model.addAttribute("passwordOldNoMatch", null);
				model.addAttribute("uploadFail", "Your password has been succesfully changed!");
				logger.info("old password matches new");
			} else {
				model.addAttribute("passwordOldNoMatch", "The password entered does not match your current password");
				logger.info("old password doesn't match new");
			} 
			Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "opened");
			model.addAttribute("openedByUser", openedByUser);
			Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
			model.addAttribute("closedByUser", closedByUser);
			}
		return "enquiryUser";
	}
	
	@GetMapping("/user/activate")
	public String activate(@RequestParam("activationToken") String token, Model model) {
		User user = userService.findByActivationToken(token);
		if (user == null) {
			model.addAttribute("invalidToken", "Oops!  This is an invalid activation link.");
		} else {
			user.setEnabled(true);
			user.setActivationToken(null);
			userService.save(user);	
			model.addAttribute("user", user);
			model.addAttribute("invalidToken", null);
			logger.info("User activated");
		}
		return "activation";		
	}
	
	@GetMapping("/user/reset")
	public String reset(@RequestParam("resetToken") String token, Model model) {
		User user = userService.findByResetToken(token);
		model.addAttribute("reset", null);
		if (user == null) {
			model.addAttribute("invalidToken", "Oops!  This is an invalid reset link.");
		} else {
			user.setEnabled(true);
			user.setResetToken(null);
			userService.save(user);	
			model.addAttribute("user", user);
			model.addAttribute("invalidToken", null);
			logger.info("User reset link sent");
		}
		return "reset";		
	}
	
	@PostMapping("/user/resetForm")
	public String resetForm(@ModelAttribute @Valid User user, BindingResult bindResult, Model model) {
		if(bindResult.hasErrors()) {
			model.addAttribute("user", user);
			logger.info("User password has not been reset");
			return "reset";
		}else {
			User userReset = userService.findById(user.getId());
			userReset.setResetToken(null);
			userReset.setPassword(passwordEncoder.encode(user.getPassword()));
			userService.save(userReset);
			model.addAttribute("reset", "Reset OK");
			logger.info("User password has been reset");
		}
		return "reset";
	}
	
	@PostMapping("/user/forgot")
	public String forgot(@ModelAttribute @Valid User user, BindingResult bindResult, Model model, HttpServletRequest request) {
		User userToReset = userService.getUserByUsername(user.getEmail());	
		if(userToReset == null) {
			model.addAttribute("forgotEmail", " " + user.getEmail());
			Enquiry enquiry = new Enquiry();
			model.addAttribute("enquiry", enquiry);
			return "resetFollowUp";
		} else {
			try {
				String resetToken = UUID.randomUUID().toString();
				String appUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort(); //development
//				String appUrl = APP_URL; //production
				SimpleMailMessage resetEmail = new SimpleMailMessage();
				resetEmail.setFrom("no-replay@domain.com");
				resetEmail.setTo(userToReset.getEmail());
				resetEmail.setSubject("Enquiry System: Password Reset");
				resetEmail.setText("To reset your password, please click the link below:\n"
											+ appUrl + "/user/reset?resetToken=" + resetToken);
				resetEmail.setFrom("noreply@domain.com");
				emailService.sendEmail(resetEmail);
				userToReset.setResetToken(resetToken);
				userService.save(userToReset);
				model.addAttribute("forgotOK", " " + userToReset.getEmail());
				logger.info("email sent to: " + userToReset.getEmail());
			}catch(Exception e) {
				model.addAttribute("forgotError", 
						"An error occurred while sending a password reset link. Please check your connection and try again.");
				logger.info("sending email failed");
			}
		}
		Enquiry enquiry = new Enquiry();
		model.addAttribute("enquiry", enquiry);
		return "resetFollowUp";
	}
	
	
}


/*	
	
	
	
	@GetMapping("/confirm")
	public String confirm(@RequestParam("token") String token, Model model) {
		User user = userService.findByConfirmationToken(token);
		if (user == null) {
			model.addAttribute("invalidToken", "Oops!  This is an invalid confirmation link.");
		} else {
			user.setEnabled(true);
			user.setConfirmationToken(null);
			userService.save(user);	
			model.addAttribute("user", user);
			logger.info("User confirmed");		}
		return "registrationSuccess";		
	}
	

	
	@GetMapping("/login")
	public String login() {
		return "login";
	}
	
	@GetMapping("/forgot")
	public String forgot(Model model) {
		FromView fromView = new FromView();
		model.addAttribute("incorrect", null);
		model.addAttribute("reset", null);
		model.addAttribute("fromView", fromView);
		return "forgot";
	}
	
	@PostMapping("/forgot")
	public String forgotForm(@ModelAttribute FromView fromView, Model model, HttpServletRequest request) {
		User userReset = userService.getUserByUsername(fromView.getText().trim());
		if(userReset != null) {
			try {
				String token = UUID.randomUUID().toString();
//				String appUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
				String appUrl = APP_URL;
				SimpleMailMessage resetEmail = new SimpleMailMessage();
				resetEmail.setFrom("no-replay@domain.com");
				resetEmail.setTo(userReset.getEmail());
				resetEmail.setSubject("Password Reset");
				resetEmail.setText("To reset your password, please click the link below:\n"
											+ appUrl + "/reset?token=" + token);
				resetEmail.setFrom("noreply@domain.com");
				emailService.sendEmail(resetEmail);
				userReset.setConfirmationToken(token);
				userService.save(userReset);
				fromView.setText(null);
				model.addAttribute("reset", "reset");
				model.addAttribute("incorrect", null);
				model.addAttribute("fromView", fromView);
			}catch(Exception e) {
				model.addAttribute("emailerror", "error");
				return "forgot";
			}
		}else {
			model.addAttribute("reset", null);
			model.addAttribute("incorrect", "incorrect");
			return "forgot";
		}
		return "forgotSent";
	}
	
	
	@GetMapping("/reset")
	public String reset(@RequestParam("token") String token, Model model) {
		User user = userService.findByConfirmationToken(token);
		System.err.println(token);
		System.err.println(user);
		if (user == null) {
			model.addAttribute("invalidToken", "Oops!  This is an invalid confirmation link.");
			return "resetError";
		} else {
			model.addAttribute("user", user);
		}
		return "reset";		
	}
	
	@PostMapping("/reset")
	public String resetForm(@ModelAttribute @Valid User user,BindingResult bindResult, Model model) {
		if(bindResult.hasErrors())
			return "reset";
		else {
			User userReset = userService.findById(user.getId());
			userReset.setConfirmationToken(null);
			userReset.setPassword(passwordEncoder.encode(user.getPassword()));
			userService.save(userReset);
			model.addAttribute("reset", "Reset OK");
		}
		return "login";
	}
	
	

	@GetMapping("/logout")
	public String logoutPage (HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null){    
			new SecurityContextLogoutHandler().logout(request, response, auth);
		}
		return "redirect:/login?logout";
	}
*/	
	






























