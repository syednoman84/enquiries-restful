package com.latidude99.web.controller;

import java.security.Principal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.mail.SendFailedException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.latidude99.model.User;

@Controller
public class UserController {
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	private static final String DEFAULT_ROLE = "ROLE_USER";
	private static final String ADMIN_ROLE = "ROLE_ADMIN";
	private static final String APP_URL = "contacts.latidude99.com";
	
	
}
/*	
	private UserService userService;
	
	@Autowired
	ContactService contactService;
	
	@Autowired
	ContactRepository contactRepository;
	
	@Autowired
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	FromView fromView;
	
	@Autowired
	ContactWrapper contactWrapper;
	
		
	@ModelAttribute("fromView")
	public FromView getFromView() {
	    return fromView;
	}
	
	@ModelAttribute("contactWrapper")
	public ContactWrapper getContactWrapper() {
	    return contactWrapper;
	}

	@GetMapping("/register")
	public String register(Model model) {
		model.addAttribute("user", new User());
		return "registration";
	}

	@PostMapping("/register")
	public String addUser(@ModelAttribute @Valid User user,	BindingResult bindResult, Model model, HttpServletRequest request) {
		if(userService.isAvailable(user)) {
			if(bindResult.hasErrors())
				return "registration";
			else {
				try {
					String token = UUID.randomUUID().toString();
//					String appUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
					String appUrl = APP_URL;
					SimpleMailMessage registrationEmail = new SimpleMailMessage();
					registrationEmail.setFrom("no-replay@domain.com");
					registrationEmail.setTo(user.getEmail());
					registrationEmail.setSubject("Registration Confirmation");
					registrationEmail.setText("To confirm your e-mail address, please click the link below:\n"
												+ appUrl + "/confirm?token=" + token);
					registrationEmail.setFrom("noreply@domain.com");
					emailService.sendEmail(registrationEmail);
					user.setEnabled(false);
					user.setConfirmationToken(token);
					userService.addWithDefaultRole(user);
					model.addAttribute("confirmationMessage", "A confirmation e-mail has been sent to " + user.getEmail());
					model.addAttribute("emailerror", null);
					logger.info("Registration succesfull");
					return "registrationConfirmation";
				}catch(Exception e) {
					model.addAttribute("emailerror", "error");
					return "registration";
				}
			}
		}else {
			Boolean notAvailable = true;
			model.addAttribute("notAvailable", notAvailable);
			return "registration";
		}
	}
	
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
	
	@GetMapping("/enabled")
	public String enabled(Model model, Principal principal, RedirectAttributes redirect) {
		String currentUserName = principal.getName();
		User currentUser = userService.getUserByUsername(currentUserName);
		redirect.addFlashAttribute("user", currentUser);
		if(currentUser.isEnabled() == true) {
			List<Contact> currentUserContacts = currentUser.getContacts();
			redirect.addFlashAttribute("currentUserContacts", currentUserContacts);
			logger.info("User enabled");
			return "redirect:/contacts";
		}else {
			return "redirect:/disabled";
		}
	}
	
	@GetMapping("/disabled")
	public String enabled(@ModelAttribute User currentUser, Model model, HttpServletRequest request) {
		model.addAttribute("user", currentUser);
		return "disabled";
	}
	
	@PostMapping("/resend")
	public String resend(@ModelAttribute User user, Model model, HttpServletRequest request) {
		try {
			User currentUser = userService.findById(user.getId());
//			String appUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
			String appUrl = APP_URL;
			SimpleMailMessage registrationEmail = new SimpleMailMessage();
			registrationEmail.setFrom("no-replay@domain.com");
			registrationEmail.setTo(currentUser.getEmail());
			registrationEmail.setSubject("Registration Confirmation");
			registrationEmail.setText("To confirm your e-mail address, please click the link below:\n"
										+ appUrl + "/confirm?token=" + currentUser.getConfirmationToken());
			registrationEmail.setFrom("noreply@domain.com");
			emailService.sendEmail(registrationEmail);
			model.addAttribute("user", currentUser);
			model.addAttribute("confirmationMessage", "A confirmation e-mail has been sent to " + currentUser.getEmail());
			logger.info("Confirmation link re-sent");
			return "registrationConfirmation";
		}catch(Exception e) {
			model.addAttribute("emailerror", "error");
			return "registration";
		}
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
	
	
	
	@GetMapping("/updateDetails")
	public String updateDetails(Model model, Principal principal) {
		ZonedDateTime currentZonedDateTime = ZonedDateTime.now();
		model.addAttribute("currentZonedDateTime", currentZonedDateTime);
		model.addAttribute("currentZone", currentZonedDateTime.getZone());
		String currentUserName = principal.getName();
		User currentUser = userService.getUserByUsername(currentUserName);
		model.addAttribute("currentUser", currentUser);
		Integer contactsTotalByUser = contactService.getTotalByUser(currentUser);
		model.addAttribute("contactsTotalByUser", contactsTotalByUser);
		List<Contact> resultContacts = new ArrayList<>();
		model.addAttribute("currentUserContacts", resultContacts);
		return "updateDetails";
	}

	
	@PostMapping("/updateDetails")
	public String updateDetails(@ModelAttribute ("currentUser") @Valid User user, BindingResult result, Model model, Principal principal, RedirectAttributes redirect) {
		if (result.hasErrors()) {
			ZonedDateTime currentZonedDateTime = ZonedDateTime.now();
			model.addAttribute("currentZonedDateTime", currentZonedDateTime);
			model.addAttribute("currentZone", currentZonedDateTime.getZone());
			String currentUserName = principal.getName();
			User currentUser = userService.getUserByUsername(currentUserName);
			model.addAttribute("currentUser", currentUser);
			Integer contactsTotalByUser = contactService.getTotalByUser(currentUser);
			model.addAttribute("contactsTotalByUser", contactsTotalByUser);
			List<Contact> resultContacts = new ArrayList<>();
			model.addAttribute("currentUserContacts", resultContacts);			
			return "updateDetails";
		}else {
			String currentUserName = principal.getName();
			User currentUser = userService.getUserByUsername(currentUserName);
			if(!currentUserName.equals(user.getEmail())){
				user.setId(currentUser.getId());
				user.setRegistered(currentUser.getRegistered());
				user.setRoles(currentUser.getRoles());
				user.setPassword(passwordEncoder.encode(user.getPassword()));
				user.setEnabled(true);
				userService.save(user);
				return "redirect:logout";
			}
			currentUser.setFirstName(user.getFirstName());
			currentUser.setLastName(user.getLastName());
			currentUser.setPassword(passwordEncoder.encode(user.getPassword()));
			userService.save(currentUser);
			model.addAttribute("currentUser", currentUser);
			}
		return "redirect:contacts";
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
	
	/* Admin tools */
/*	@PostMapping("/userPasswordReset")
	public String userPasswordReset(@ModelAttribute User user, Model model, HttpServletRequest request, Principal principal) {
		ZonedDateTime currentZonedDateTime = ZonedDateTime.now();
		model.addAttribute("currentZonedDateTime", currentZonedDateTime);
		model.addAttribute("currentZone", currentZonedDateTime.getZone());
		String currentUserName = principal.getName();
		User currentUser = userService.getUserByUsername(currentUserName);
		model.addAttribute("currentUser", currentUser);
		Integer contactsTotalByUser = contactService.getTotalByUser(currentUser);
		model.addAttribute("contactsTotalByUser", contactsTotalByUser);
		List<Contact> resultContacts = new ArrayList<>();
		model.addAttribute("currentUserContacts", resultContacts);
		if(request.isUserInRole(ADMIN_ROLE)) {
			List<User> users = userService.getAllUsersNoAdmins();
			model.addAttribute("users", users);
			logger.info("users size: " + users.size());
		}
		User userReset = userService.findById(user.getId());
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
				model.addAttribute("reset", "reset");
				model.addAttribute("incorrect", null);
				logger.info("email sent");
			}catch(Exception e) {
				model.addAttribute("emailerror", "error");
				logger.info("email failed");
				return "adminPanel";
			}
		}else {
			model.addAttribute("reset", null);
			model.addAttribute("incorrect", "incorrect");
			logger.info("user's email not found");
			return "adminPanel";
		}
		return "adminPanel";
	}
	
	@PostMapping("/userEnable")
	public String confirm(@ModelAttribute User user, Model model, HttpServletRequest request, Principal principal) {
		ZonedDateTime currentZonedDateTime = ZonedDateTime.now();
		model.addAttribute("currentZonedDateTime", currentZonedDateTime);
		model.addAttribute("currentZone", currentZonedDateTime.getZone());
		String currentUserName = principal.getName();
		User currentUser = userService.getUserByUsername(currentUserName);
		model.addAttribute("currentUser", currentUser);
		Integer contactsTotalByUser = contactService.getTotalByUser(currentUser);
		model.addAttribute("contactsTotalByUser", contactsTotalByUser);
		List<Contact> resultContacts = new ArrayList<>();
		model.addAttribute("currentUserContacts", resultContacts);
		if(request.isUserInRole(ADMIN_ROLE)) {
			List<User> users = userService.getAllUsersNoAdmins();
			model.addAttribute("users", users);
			logger.info("users size: " + users.size());
		}
		User userReset = userService.findById(user.getId());
		if(userReset.isEnabled()) {
			userReset.setEnabled(false);
		} else {
			userReset.setEnabled(true);
		}
		userReset.setConfirmationToken(null);
		userService.save(userReset);	
//		model.addAttribute("user", userReset);
		logger.info("User enabled/disabled");
		return "adminPanel";	
	}
	
	@PostMapping("/userSendConfirmation")
	public String userSendConfirmation(@ModelAttribute User user, Model model, HttpServletRequest request, Principal principal) {
		ZonedDateTime currentZonedDateTime = ZonedDateTime.now();
		model.addAttribute("currentZonedDateTime", currentZonedDateTime);
		model.addAttribute("currentZone", currentZonedDateTime.getZone());
		String currentUserName = principal.getName();
		User currentUser = userService.getUserByUsername(currentUserName);
		model.addAttribute("currentUser", currentUser);
		Integer contactsTotalByUser = contactService.getTotalByUser(currentUser);
		model.addAttribute("contactsTotalByUser", contactsTotalByUser);
		List<Contact> resultContacts = new ArrayList<>();
		model.addAttribute("currentUserContacts", resultContacts);
		if(request.isUserInRole(ADMIN_ROLE)) {
			List<User> users = userService.getAllUsersNoAdmins();
			model.addAttribute("users", users);
			logger.info("users size: " + users.size());
		}
		try {
			User resetUser = userService.findById(user.getId());
//			String appUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
			String appUrl = APP_URL;
			String token = UUID.randomUUID().toString();
			SimpleMailMessage registrationEmail = new SimpleMailMessage();
			registrationEmail.setFrom("no-replay@domain.com");
			registrationEmail.setTo(resetUser.getEmail());
			registrationEmail.setSubject("Registration Confirmation");
			registrationEmail.setText("To confirm your e-mail address, please click the link below:\n"
										+ appUrl + "/confirm?token=" + token);
			registrationEmail.setFrom("noreply@domain.com");
			emailService.sendEmail(registrationEmail);
			resetUser.setConfirmationToken(token);
			userService.save(resetUser);
			model.addAttribute("user", resetUser);
			model.addAttribute("confirmationMessage", "A confirmation e-mail has been sent to " + resetUser.getEmail());
			logger.info("Confirmation link re-sent");
			return "adminPanel";
		}catch(Exception e) {
			model.addAttribute("emailerror", "error");
		}
		return "adminPanel";
	}
*/





























