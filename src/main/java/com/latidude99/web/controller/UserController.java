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


    /*
     * user page controller, password change form
     */
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

    /*
     * user password update
     */
    @PostMapping("/user/update")
    public String updateDetails(@ModelAttribute("currentUser") @Valid User user, BindingResult result,
                                Model model, Principal principal) {
        if (result.hasErrors()) {
            String currentUserName = principal.getName();
            User currentUser = userService.getUserByUsername(currentUserName);
            model.addAttribute("currentUser", currentUser);
            Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "opened");
            model.addAttribute("openedByUser", openedByUser);
            Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
            model.addAttribute("closedByUser", closedByUser);
            logger.info("password validation errors");
            result.getAllErrors().forEach(e -> logger.info(e.toString()));
            return "enquiryUser";
        } else {
            String currentUserName = principal.getName();
            User currentUser = userService.getUserByUsername(currentUserName);
            if (passwordEncoder.matches(user.getPassword(), currentUser.getPassword())) {
                currentUser.setPassword(passwordEncoder.encode(user.getPasswordNew()));
                userService.save(currentUser);
                model.addAttribute("currentUser", currentUser);
                model.addAttribute("passwordOldNoMatch", null);
                model.addAttribute("uploadFail",
                        "Your password has been succesfully changed!");
                logger.info("old password matches new");
            } else {
                model.addAttribute("passwordOldNoMatch",
                        "The password entered does not match your current password");
                logger.info("old password doesn't match new");
            }
            Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "opened");
            model.addAttribute("openedByUser", openedByUser);
            Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
            model.addAttribute("closedByUser", closedByUser);
        }
        return "enquiryUser";
    }

    /*
     * user activation after activation link has been clicked in email
     */
    @GetMapping("/user/activate")
    public String activate(@RequestParam("activationToken") String token, Model model) {
        User user = userService.findByActivationToken(token);
        if (user == null) {
            model.addAttribute("invalidToken",
                    "Oops!  This is an invalid activation link.");
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


    /*
     * user reset password form after password reset link has been clicked in email
     */
    @GetMapping("/user/reset")
    public String reset(@RequestParam("resetToken") String token, Model model) {
        User user = userService.findByResetToken(token);
        model.addAttribute("reset", null);
        if (user == null) {
            model.addAttribute("invalidToken",
                    "Oops!  This is an invalid reset link.");
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

    /*
     * user reset password form processing
     */
    @PostMapping("/user/resetForm")
    public String resetForm(@ModelAttribute @Valid User user, BindingResult bindResult, Model model) {
        if (bindResult.hasErrors()) {
            model.addAttribute("user", user);
            logger.info("User password has not been reset");
            return "reset";
        } else {
            User userReset = userService.findById(user.getId());
            userReset.setResetToken(null);
            userReset.setPassword(passwordEncoder.encode(user.getPassword()));
            userService.save(userReset);
            model.addAttribute("reset", "Reset OK");
            logger.info("User password has been reset");
        }
        return "reset";
    }

    /*
     * user forgot password form processing
     */
    @PostMapping("/user/forgot")
    public String forgot(@ModelAttribute @Valid User user, BindingResult bindResult, Model model,
                         HttpServletRequest request) {
        User userToReset = userService.getUserByUsername(user.getEmail());
        if (userToReset == null) {
            model.addAttribute("forgotEmail", " " + user.getEmail());
            Enquiry enquiry = new Enquiry();
            model.addAttribute("enquiry", enquiry);
            return "resetFollowUp";
        } else {
            try {
                String resetToken = UUID.randomUUID().toString();
                String appUrl = request.getScheme() + "://" + request.getServerName() +
                        ":" + request.getServerPort(); //development
//				String appUrl = APP_URL; //production
                SimpleMailMessage resetEmail = new SimpleMailMessage();
                resetEmail.setFrom("no-replay@domain.com");
                resetEmail.setTo(userToReset.getEmail());
                resetEmail.setSubject("Enquiry System: Password Reset");
                resetEmail.setText("To reset your password, please click the link below:\n" +
                        appUrl + "/user/reset?resetToken=" + resetToken);
                resetEmail.setFrom("noreply@domain.com");
                emailService.sendEmail(resetEmail);
                userToReset.setResetToken(resetToken);
                userService.save(userToReset);
                model.addAttribute("forgotOK", " " + userToReset.getEmail());
                logger.info("email sent to: " + userToReset.getEmail());
            } catch (Exception e) {
                model.addAttribute("forgotError", "An error occurred while sending a password reset link. Please check your connection and try again.");
                logger.info("sending email failed");
            }
        }
        Enquiry enquiry = new Enquiry();
        model.addAttribute("enquiry", enquiry);
        return "resetFollowUp";
    }


}












