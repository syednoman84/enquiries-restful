/**
 * Copyright (C) 2018-2019  Piotr Czapik.
 *
 * @author Piotr Czapik
 * <p>
 * This file is part of EnquirySystem.
 * EnquirySystem is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * EnquirySystem is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with EnquirySystem.  If not, see <http://www.gnu.org/licenses/>
 * or write to: latidude99@gmail.com
 */

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
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy  HH:mm");

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


    public void sendSimpleMessage(Enquiry enquiry, User user) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

        Context context = new Context();
        context.setVariable("enquiry", enquiry);
        String html = templateEngine.process("enquiryPageEmail", context);

        helper.setTo(user.getEmail());
        helper.setText(html, true);
        helper.setSubject("Enquiry " + enquiry.getId() + ", " +
                enquiry.getName() + ", " + enquiry.getCreatedDate().format(formatter));
        helper.setFrom("map.centre@gmail.com");

        emailSender.send(message);
    }

    public void sendSimpleMessage(Enquiry enquiry, String email) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

        Context context = new Context();
        context.setVariable("enquiry", enquiry);
        String html = templateEngine.process("enquiryPageEmail", context);

        helper.setTo(email);
        helper.setText(html, true);
        helper.setSubject("Enquiry " + enquiry.getId() + ", " +
                enquiry.getName() + ", " + enquiry.getCreatedDate().format(formatter));
        helper.setFrom("map.centre@gmail.com");

        emailSender.send(message);
    }

}








