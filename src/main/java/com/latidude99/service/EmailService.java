package com.latidude99.service;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring5.SpringTemplateEngine;

import com.latidude99.model.Enquiry;
//import com.latidude99.model.Mail;
import com.latidude99.model.User;

//import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.context.Context;
import javax.mail.MessagingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;


@Service
public class EmailService {

	  private JavaMailSender mailSender;
	
	  @Autowired
	  public EmailService(JavaMailSender mailSender) {
		  this.mailSender = mailSender;
	  }
	  
	  @Async
	  public void sendEmail(SimpleMailMessage email) {
		  mailSender.send(email);
	  }
	  
	  @Autowired
	  private JavaMailSender emailSender;
	
	  @Autowired
	  private SpringTemplateEngine templateEngine;
	
	
	  public void sendSimpleMessage(/*Mail mail, */Enquiry enquiry, User user) throws MessagingException, IOException {
	      MimeMessage message = emailSender.createMimeMessage();
	      MimeMessageHelper helper = new MimeMessageHelper(message,
	              MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
	              StandardCharsets.UTF_8.name());
	
//	      helper.addAttachment("logo.png", new ClassPathResource("memorynotfound-logo.png"));
	
	      Context context = new Context();
//	      context.setVariables(mail.getModel());
	      context.setVariable("enquiry", enquiry);
	      String html = templateEngine.process("enquiryPageEmail", context);
	
	      helper.setTo(user.getEmail());
	      helper.setText(html, true);
	      helper.setSubject("Enquiry " + enquiry.getId() + ", " + enquiry.getName() + ", " + enquiry.getCreatedDate().format(formatter));
	      helper.setFrom("map.centre@gmail.com");
	
	      emailSender.send(message);
	  }
	  
	  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy  HH:mm");
	  
	  
}

/*
 
  Mail mail = new Mail();
        mail.setFrom("no-reply@memorynotfound.com");
        mail.setTo("info@memorynotfound.com");
        mail.setSubject("Sending Email with Thymeleaf HTML Template Example");

        Map model = new HashMap();
        model.put("name", "Memorynotfound.com");
        model.put("location", "Belgium");
        model.put("signature", "https://memorynotfound.com");
        mail.setModel(model);

        emailService.sendSimpleMessage(mail);
 
 */























