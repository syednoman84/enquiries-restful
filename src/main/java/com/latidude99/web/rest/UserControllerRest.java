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

package com.latidude99.web.rest;

import com.latidude99.model.Enquiry;
import com.latidude99.model.User;
import com.latidude99.service.EmailService;
import com.latidude99.service.EnquiryService;
import com.latidude99.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Validated
public class UserControllerRest {
    private static final Logger logger = LoggerFactory.getLogger(UserControllerRest.class);
    private static final String APP_URL = "enquiry.latidude99.com";


    @Autowired
    private UserService userService;

    @Autowired
    EnquiryService enquiryService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping(path = "/error/reset")
    public ResponseEntity<?> errorReset() {
        ErrorRest errorRest = new ErrorRest(HttpStatus.EXPECTATION_FAILED,
                "Invalid Reset Token",
                "not authorized");
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(errorRest);
    }

    /*
     * user password update
     */
    @PostMapping(path = "/user/update")
    public ResponseEntity<?> updateDetails(@RequestParam @Size(min = 6) String passwordNew,
                                           @RequestParam @NotBlank  String passwordOld,
                                            Principal principal) {
        String currentUserName = principal.getName();
        User currentUser = userService.getUserByUsername(currentUserName);
        if (passwordEncoder.matches(passwordOld, currentUser.getPassword())) {
            currentUser.setPassword(passwordEncoder.encode(passwordNew));
            userService.save(currentUser);
        } else {
            ErrorRest errorRest = new ErrorRest(HttpStatus.EXPECTATION_FAILED,
                    "the password entered does not match your current password",
                    "not authorized");
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(errorRest);
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("password changed");
    }

    /*
     * user activation after activation link has been clicked in email
     */
    @GetMapping(path = "/user/activate")
    public ResponseEntity<?> activate(@RequestParam("activationToken") String token) {
        User user = userService.findByActivationToken(token);
        if (user == null) {
            ErrorRest errorRest = new ErrorRest(HttpStatus.EXPECTATION_FAILED,
                    "invalid activation token",
                    "not authorized");
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(errorRest);
        } else {
            user.setEnabled(true);
            user.setActivationToken(null);
            userService.save(user);
        }
        String message = "user " + user.getEmail() + " activated";
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(message);
    }


    /*
     * Returns reset token when password reset link has been clicked in email and the token is valid
     */
    @GetMapping(path = "/user/reset")
    public ResponseEntity<?> reset(@RequestParam("resetToken") String token, Model model) {
        User user = userService.findByResetToken(token);
        if (user != null) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(token);

        } else {
            ErrorRest errorRest = new ErrorRest(HttpStatus.EXPECTATION_FAILED,
                    "invalid reset token",
                    "not authorized");
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(errorRest);
        }
    }

    /*
     * Processes user reset password
     */
    @PostMapping(path = "/user/reset", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> resetForm(@RequestParam String token,
                            @RequestParam @Size(min = 6) String password) {
        User user = userService.findByResetToken(token);
        if(user != null && token.equals(user.getResetToken())){
            user = userService.findById(user.getId());
            user.setResetToken(null);
            user.setPassword(passwordEncoder.encode(password));
            userService.save(user);
            return ResponseEntity.status(HttpStatus.OK).body(user);
        }else{
            ErrorRest errorRest = new ErrorRest(HttpStatus.EXPECTATION_FAILED,
                    "invalid reset token",
                    "not authorized");
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(errorRest);
        }
    }

    /*
     * Sends an email with a reset token (forgot password form)
     */
    @PostMapping("/user/forgot")
    public ResponseEntity<?> forgot(@RequestParam @Email String email,
                         HttpServletRequest request) {
        User userToReset = userService.getUserByUsername(email);
        if (userToReset != null) {
            try {
                String resetToken = UUID.randomUUID().toString();
                String appUrl = request.getScheme() + "://" + request.getServerName() +
                        ":" + request.getServerPort(); //development
//				String appUrl = APP_URL; //production
                SimpleMailMessage resetEmail = new SimpleMailMessage();
                resetEmail.setFrom("no-replay@domain.com");
                resetEmail.setTo(email);
                resetEmail.setSubject("Enquiry System: Password Reset");
                resetEmail.setText("To reset your password, please click the link below:\n" +
                        appUrl + "/api/user/reset?token=" + resetToken);
                resetEmail.setFrom("noreply@domain.com");
                emailService.sendEmail(resetEmail);
                userToReset.setResetToken(resetToken);
                userService.save(userToReset);
                logger.info("email sent to: " + userToReset.getEmail());
            } catch (Exception e) {
                ErrorRest errorRest = new ErrorRest(HttpStatus.EXPECTATION_FAILED,
                        "Error occurred while sending an email to: " + email,
                        e.getLocalizedMessage());
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(errorRest);
            }
        } else {
            ErrorRest errorRest = new ErrorRest(HttpStatus.NOT_FOUND,
                    "no register user with the email address: " + email,
                    "resource not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorRest);
        }
        return ResponseEntity.status(HttpStatus.OK).body(userToReset);
    }


}












