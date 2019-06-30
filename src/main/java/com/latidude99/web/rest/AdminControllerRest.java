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

import com.latidude99.model.Role;
import com.latidude99.model.User;
import com.latidude99.model.UserRole;
import com.latidude99.service.EmailService;
import com.latidude99.service.EnquiryService;
import com.latidude99.service.UserRoleService;
import com.latidude99.service.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api")
@Validated
public class AdminControllerRest {
    private static final Logger logger = LoggerFactory.getLogger(AdminControllerRest.class);
    private static final String APP_URL = "enquiry.latidude99.com";

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    EnquiryService enquiryService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;



    /*
     * Allows admin users to add other users/admins privileges
     */
    @PostMapping(path = "/admin/{userId}/priviledges/add")
    @PreAuthorize("hasRole('ROLE_ADMIN') OR hasRole('ROLE_APPADMIN')")
    public ResponseEntity<?> addPrivileges(@PathVariable long userId,
                             @RequestParam List<String> priviledges,
                             Principal principal) {
        priviledges.forEach(p -> p.toUpperCase());
        User currentUser = userService.getUserByUsername(principal.getName());
        User userToUpdate = userService.findById(userId);
        String responseMessage = "";
        if(userToUpdate != null){
            if(userToUpdate.getEmail().equals(currentUser.getEmail())){
                ErrorRest errorRest = new ErrorRest(HttpStatus.FORBIDDEN,
                        "Users cannot modify their own priviledges",
                        "Restricted operation");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorRest);
            }

            Map<String,UserRole> allRoles = userService.rolesToMap();
            if(currentUser.getRoles().contains(allRoles.get("appadmin"))) { // appadmin can modify all users
                userToUpdate = userService.setRoles(userToUpdate, priviledges, allRoles, "add");

            } else if(!userToUpdate.getRoles().contains(allRoles.get("appadmin"))) { // admin can modify admin, user
                if(priviledges.contains("APPADMIN")){
                    priviledges.remove("APPADMIN"); // admin cannot add appadmin rights
                    responseMessage = "APPADMIN rights were removed form the request, ADMIN cannot add APPADMIN " +
                            "rights\r\n";
                }
                if(priviledges.size() < 1){
                    ErrorRest errorRest = new ErrorRest(HttpStatus.FORBIDDEN,
                            "Isufficient priviledges: a user with ADMIN rights " +
                                    "cannot add APPADMIN rights",
                            "Insufficient rights for the operation");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorRest);
                }
                userToUpdate = userService.setRoles(userToUpdate, priviledges, allRoles, "add");
            } else{ // admin cannot modify appadmin
                ErrorRest errorRest = new ErrorRest(HttpStatus.FORBIDDEN,
                        "Isufficient priviledges: a user with ADMIN rights " +
                                "cannot modify a user with APPADMIN rights",
                        "Insufficient rights for the operation");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorRest);
            }
            userService.save(userToUpdate);

        }else{
            ErrorRest errorRest = new ErrorRest(HttpStatus.NOT_FOUND,
                    "No user found with id: " + userId,
                    "resource not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorRest);
        }
        String priviledgesStatus = userService.rolesToString(userToUpdate);
        responseMessage = responseMessage +
                "User " +  userToUpdate.getEmail() + " has priviledges: " + priviledgesStatus;
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(responseMessage);
    }

    /*
     * Allows admin users to remove other users/admins privileges
     */
    @PostMapping(path = "/admin/{userId}/priviledges/remove")
    @PreAuthorize("hasRole('ROLE_ADMIN') OR hasRole('ROLE_APPADMIN')")
    public ResponseEntity<?> removePrivileges(@PathVariable long userId,
                                        @RequestParam List<String> priviledges,
                                        Principal principal) {
        priviledges.forEach(p -> p.toUpperCase());
        User currentUser = userService.getUserByUsername(principal.getName());
        User userToUpdate = userService.findById(userId);
        String responseMessage = "";
        if(userToUpdate != null){
            if(userToUpdate.getEmail().equals(currentUser.getEmail())){
                ErrorRest errorRest = new ErrorRest(HttpStatus.FORBIDDEN,
                        "Users cannot modify their own priviledges",
                        "Restricted operation");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorRest);
            }

            Map<String,UserRole> allRoles = userService.rolesToMap();
            if(currentUser.getRoles().contains(allRoles.get("appadmin"))) { // appadmin can modify all users
                userToUpdate = userService.setRoles(userToUpdate, priviledges, allRoles, "remove");

            } else if(!userToUpdate.getRoles().contains(allRoles.get("appadmin"))) { // admin can modify admin, user
                if(priviledges.contains("APPADMIN")){
                    priviledges.remove("APPADMIN"); // admin cannot remove appadmin rights
                    responseMessage = "APPADMIN rights were removed form the request," +
                            " ADMIN cannot remove APPADMIN rights";
                }
                if(priviledges.size() < 1){
                    ErrorRest errorRest = new ErrorRest(HttpStatus.FORBIDDEN,
                            "Isufficient priviledges: a user with ADMIN rights " +
                                    "cannot remove APPADMIN rights",
                            "Insufficient rights for the operation");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorRest);
                }
                userToUpdate = userService.setRoles(userToUpdate, priviledges, allRoles, "remove");
            } else{ // admin cannot modify appadmin
                ErrorRest errorRest = new ErrorRest(HttpStatus.FORBIDDEN,
                        "Isufficient priviledges: a user with ADMIN rights " +
                                "cannot modify a user with APPADMIN rights",
                        "Insufficient rights for the operation");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorRest);
            }
            userService.save(userToUpdate);

        }else{
            ErrorRest errorRest = new ErrorRest(HttpStatus.NOT_FOUND,
                    "No user found with id: " + userId,
                    "resource not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorRest);
        }
        String priviledgesStatus = userService.rolesToString(userToUpdate);
        responseMessage = responseMessage +
                "User " +  userToUpdate.getEmail() + " has priviledges: " + priviledgesStatus;
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(responseMessage);
    }

    /*
     * Allows admins to block/unblock users
     * Works as a switch: each request changes for the opposite to the current state
     */
    @PostMapping(path = "/admin/{userId}/block")
    @PreAuthorize("hasRole('ROLE_ADMIN') OR hasRole('ROLE_APPADMIN')")
    public ResponseEntity<?> blockUnblock(@PathVariable long userId, Principal principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        User userToUpdate = userService.findById(userId);
        String responseMessage = "";

        if(userToUpdate != null){
            if(userToUpdate.getEmail().equals(currentUser.getEmail())){
                ErrorRest errorRest = new ErrorRest(HttpStatus.FORBIDDEN,
                        "Users cannot modify themselves",
                        "Restricted operation");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorRest);
            }

            UserRole roleAdmin = userRoleService.getUserRole(Role.ADMIN.getText());
            UserRole roleAppAdmin = userRoleService.getUserRole(Role.APPADMIN.getText());
            if (currentUser.getRoles().contains(roleAdmin) &&
                    !currentUser.getRoles().contains(roleAppAdmin)) {
                if (userToUpdate.getRoles().contains(roleAppAdmin)){
                    ErrorRest errorRest = new ErrorRest(HttpStatus.FORBIDDEN,
                            "Isufficient priviledges: a user with ADMIN rights " +
                                    "cannot modify a user with APPADMIN rights",
                            "Insufficient rights for the operation");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorRest);
                }
                if (!userToUpdate.getRoles().contains(roleAdmin) &&
                        !userToUpdate.getRoles().contains(roleAppAdmin) &&
                        userToUpdate.isBlocked()) {
                    userToUpdate.setBlocked(false);
                    userService.save(userToUpdate);
                } else if (!userToUpdate.getRoles().contains(roleAdmin) &&
                        !userToUpdate.getRoles().contains(roleAppAdmin) &&
                        !userToUpdate.isBlocked()) {
                    userToUpdate.setBlocked(true);
                    userService.save(userToUpdate);
                }
            }else if (currentUser.getRoles().contains(roleAppAdmin)) {
                if (!userToUpdate.getRoles().contains(roleAppAdmin) && userToUpdate.isBlocked()) {
                    userToUpdate.setBlocked(false);
                    userService.save(userToUpdate);
                } else if (!userToUpdate.getRoles().contains(roleAppAdmin) && !userToUpdate.isBlocked()) {
                    userToUpdate.setBlocked(true);
                    userService.save(userToUpdate);
                }
            }else{ // in case @PreAuth0rize is removed
                ErrorRest errorRest = new ErrorRest(HttpStatus.FORBIDDEN,
                        "Your priviledges: " + userService.rolesToString2(currentUser) +
                                " do not allow for this operation",
                        "Insufficient rights for the operation");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorRest);
            }

        }else{
            ErrorRest errorRest = new ErrorRest(HttpStatus.NOT_FOUND,
                    "No user found with id: " + userId,
                    "resource not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorRest);
        }
        responseMessage = responseMessage +
                "User " +  userToUpdate.getEmail() + " is blocked: " + userToUpdate.isBlocked();
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(responseMessage);
    }


    /*
     * Allows admins to enable/disable users
     * Works as a switch: each request changes for the opposite to the current state
     */
    @PostMapping(path = "/admin/{userId}/disable")
    @PreAuthorize("hasRole('ROLE_ADMIN') OR hasRole('ROLE_APPADMIN')")
    public ResponseEntity<?> enableDisable(@PathVariable long userId, Principal principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        User userToUpdate = userService.findById(userId);

        if(userToUpdate != null){
            if(userToUpdate.getEmail().equals(currentUser.getEmail())){
                ErrorRest errorRest = new ErrorRest(HttpStatus.FORBIDDEN,
                        "Users cannot modify themselves",
                        "Restricted operation");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorRest);
            }

            UserRole roleAdmin = userRoleService.getUserRole(Role.ADMIN.getText());
            UserRole roleAppAdmin = userRoleService.getUserRole(Role.APPADMIN.getText());
            if (currentUser.getRoles().contains(roleAdmin) &&
                    !currentUser.getRoles().contains(roleAppAdmin)) {
                if (userToUpdate.getRoles().contains(roleAppAdmin)){
                    ErrorRest errorRest = new ErrorRest(HttpStatus.FORBIDDEN,
                            "Isufficient priviledges: a user with ADMIN rights " +
                                    "cannot modify a user with APPADMIN rights",
                            "Insufficient rights for the operation");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorRest);
                }
                if (!userToUpdate.getRoles().contains(roleAdmin) &&
                        !userToUpdate.getRoles().contains(roleAppAdmin) &&
                        userToUpdate.isEnabled()) {
                    userToUpdate.setEnabled(false);
                    userService.save(userToUpdate);
                } else if (!userToUpdate.getRoles().contains(roleAdmin) &&
                        !userToUpdate.getRoles().contains(roleAppAdmin) &&
                        !userToUpdate.isEnabled()) {
                    userToUpdate.setEnabled(true);
                    userService.save(userToUpdate);
                }
            }else if (currentUser.getRoles().contains(roleAppAdmin)) {
                if (!userToUpdate.getRoles().contains(roleAppAdmin) && userToUpdate.isEnabled()) {
                    userToUpdate.setEnabled(false);
                    userService.save(userToUpdate);
                } else if (!userToUpdate.getRoles().contains(roleAppAdmin) && !userToUpdate.isEnabled()) {
                    userToUpdate.setEnabled(true);
                    userService.save(userToUpdate);
                }
            }else{ // in case @Secured is removed
                ErrorRest errorRest = new ErrorRest(HttpStatus.FORBIDDEN,
                        "Your priviledges: " + userService.rolesToString2(currentUser) +
                                " do not allow for this operation",
                        "Insufficient rights for the operation");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorRest);
            }

        }else{
            ErrorRest errorRest = new ErrorRest(HttpStatus.NOT_FOUND,
                    "No user found with id: " + userId,
                    "resource not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorRest);
        }
        String responseMessage = "User " +  userToUpdate.getEmail() +
                " is enabled (activated): " + userToUpdate.isEnabled();
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(responseMessage);
    }

    /*
     * Allows admins to reset users' passwords and send a password reset link
     */
    @PostMapping(path = "/admin/{userId}/reset")
    @PreAuthorize("hasRole('ROLE_ADMIN') OR hasRole('ROLE_APPADMIN')")
    public ResponseEntity<?> resetEmail(@PathVariable long userId, HttpServletRequest request, Principal principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        User userToReset = userService.findById(userId);
        UserRole roleAdmin = userRoleService.getUserRole(Role.ADMIN.getText());
        UserRole roleAppAdmin = userRoleService.getUserRole(Role.APPADMIN.getText());
        String responseMessage = "";

        if(userToReset != null){
            if(userToReset.getEmail().equals(currentUser.getEmail())){
                ErrorRest errorRest = new ErrorRest(HttpStatus.FORBIDDEN,
                        "Users cannot modify themselves",
                        "Restricted operation");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorRest);
            }
            if (request.isUserInRole(Role.ADMIN.getText())) {
                if (!userToReset.getRoles().contains(roleAdmin) &&
                        !userToReset.getRoles().contains(roleAppAdmin)) {
                    try {
                        String resetToken = UUID.randomUUID().toString();
                        String appUrl = request.getScheme() + "://" +
                                request.getServerName() + ":" + request.getServerPort(); //development
//					String appUrl = APP_URL;                                        //production
                        SimpleMailMessage resetEmail = new SimpleMailMessage();
                        resetEmail.setFrom("no-replay@domain.com");
                        resetEmail.setTo(userToReset.getEmail());
                        resetEmail.setSubject("Enquiry System: Password Reset");
                        resetEmail.setText("To reset your password, please click the link below:\n" +
                                appUrl + "/api/user/reset?resetToken=" + resetToken);
                        resetEmail.setFrom("noreply@domain.com");
                        emailService.sendEmail(resetEmail);
                        userToReset.setResetToken(resetToken);
                        userService.save(userToReset);
                    } catch (Exception e) {
                        ErrorRest errorRest = new ErrorRest(HttpStatus.EXPECTATION_FAILED,
                                "sending email with password reset link to " +
                                        userToReset.getEmail() + " failed",
                                "email error");
                        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(errorRest);
                    }
                } else {
                    ErrorRest errorRest = new ErrorRest(HttpStatus.FORBIDDEN,
                            "Your priviledges: " + userService.rolesToString2(currentUser) +
                                    " do not allow for this operation",
                            "Insufficient rights for the operation");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorRest);
                }
            } else if (request.isUserInRole(Role.APPADMIN.getText())) {
                if (!userToReset.getRoles().contains(roleAppAdmin)) {
                    try {
                        String resetToken = UUID.randomUUID().toString();
                        String appUrl = request.getScheme() + "://" +
                                request.getServerName() + ":" + request.getServerPort(); //development
//					String appUrl = APP_URL;                                         //production
                        SimpleMailMessage resetEmail = new SimpleMailMessage();
                        resetEmail.setFrom("no-replay@domain.com");
                        resetEmail.setTo(userToReset.getEmail());
                        resetEmail.setSubject("Enquiry System: Password Reset");
                        resetEmail.setText("To reset your password, please click the link below:\n" +
                                appUrl + "/api/user/reset?resetToken=" + resetToken);
                        resetEmail.setFrom("noreply@domain.com");
                        emailService.sendEmail(resetEmail);
                        userToReset.setResetToken(resetToken);
                        userService.save(userToReset);
                    } catch (Exception e) {
                        ErrorRest errorRest = new ErrorRest(HttpStatus.EXPECTATION_FAILED,
                                "sending email with password reset link to " +
                                        userToReset.getEmail() + " failed",
                                "email error");
                        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(errorRest);
                    }
                } else {
                    ErrorRest errorRest = new ErrorRest(HttpStatus.FORBIDDEN,
                            "Your priviledges: " + userService.rolesToString2(currentUser) +
                                    " do not allow for this operation",
                            "Insufficient rights for the operation");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorRest);
                }
            }
        } else {
            ErrorRest errorRest = new ErrorRest(HttpStatus.NOT_FOUND,
                    "No user found with id: " + userId,
                    "resource not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorRest);
        }

        responseMessage = "email with password reset link sent to: " +
                userToReset.getEmail();
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(responseMessage);
    }

    /*
     * Allows admin users to re-send an activation link
     */
    @PostMapping(path = "/admin/{userId}/activate")
    @PreAuthorize("hasRole('ROLE_ADMIN') OR hasRole('ROLE_APPADMIN')")
    public ResponseEntity<?> activationEmail(@PathVariable long userId, HttpServletRequest request, Principal
            principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        User userToActivate = userService.findById(userId);
        UserRole roleAdmin = userRoleService.getUserRole(Role.ADMIN.getText());
        UserRole roleAppAdmin = userRoleService.getUserRole(Role.APPADMIN.getText());
        String responseMessage = "";

        if(userToActivate != null) {
            if (userToActivate.getEmail().equals(currentUser.getEmail())) {
                ErrorRest errorRest = new ErrorRest(HttpStatus.FORBIDDEN,
                        "Users cannot modify themselves",
                        "Restricted operation");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorRest);
            }

            if (request.isUserInRole(Role.ADMIN.getText())) {
                if (!userToActivate.getRoles().contains(roleAdmin) &&
                        !userToActivate.getRoles().contains(roleAppAdmin)) {
                    try {
                        String activationToken = UUID.randomUUID().toString();
                        String appUrl = request.getScheme() + "://" +
                                request.getServerName() + ":" + request.getServerPort(); //development
//					String appUrl = APP_URL;                                         //production
                        SimpleMailMessage activationEmail = new SimpleMailMessage();
                        activationEmail.setFrom("no-replay@domain.com");
                        activationEmail.setTo(userToActivate.getEmail());
                        activationEmail.setSubject("Enquiry System: Account Activation");
                        activationEmail.setText("To activate your account, please click the link below:\n" +
                                appUrl + "/api/user/activate?activationToken=" + activationToken);
                        activationEmail.setFrom("noreply@domain.com");
                        emailService.sendEmail(activationEmail);
                        userToActivate.setActivationToken(activationToken);
                        userService.save(userToActivate);
                    } catch (Exception e) {
                        ErrorRest errorRest = new ErrorRest(HttpStatus.EXPECTATION_FAILED,
                                "sending email activation link to " +
                                        userToActivate.getEmail() + " failed",
                                "email error");
                        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(errorRest);
                    }
                } else {
                    ErrorRest errorRest = new ErrorRest(HttpStatus.FORBIDDEN,
                            "Your priviledges: " + userService.rolesToString2(currentUser) +
                                    " do not allow for this operation",
                            "Insufficient rights for the operation");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorRest);
                }
            } else if (request.isUserInRole(Role.APPADMIN.getText())) {
                try {
                    String activationToken = UUID.randomUUID().toString();
                    String appUrl = request.getScheme() + "://" +
                            request.getServerName() + ":" + request.getServerPort(); //development
//				String appUrl = APP_URL;                                        //production
                    SimpleMailMessage activationEmail = new SimpleMailMessage();
                    activationEmail.setFrom("no-replay@domain.com");
                    activationEmail.setTo(userToActivate.getEmail());
                    activationEmail.setSubject("Enquiry System: Account Activation");
                    activationEmail.setText("To activate your account, please click the link below:\n" +
                            appUrl + "/api/user/activate?activationToken=" + activationToken);
                    activationEmail.setFrom("noreply@domain.com");
                    emailService.sendEmail(activationEmail);
                    userToActivate.setActivationToken(activationToken);
                    userService.save(userToActivate);
                } catch (Exception e) {
                    ErrorRest errorRest = new ErrorRest(HttpStatus.EXPECTATION_FAILED,
                            "sending email activation link to " +
                                    userToActivate.getEmail() + " failed",
                            "email error");
                    return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(errorRest);
                }
            } else {
                ErrorRest errorRest = new ErrorRest(HttpStatus.FORBIDDEN,
                        "Your priviledges: " + userService.rolesToString2(currentUser) +
                                " do not allow for this operation",
                        "Insufficient rights for the operation");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorRest);
            }
        } else {
            ErrorRest errorRest = new ErrorRest(HttpStatus.NOT_FOUND,
                    "No user found with id: " + userId,
                    "resource not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorRest);
        }
        responseMessage = "email with activation link sent to: " +
                userToActivate.getEmail();
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(responseMessage);
    }

    /*
     * Processes adding new users
     */
    @PostMapping(path = "/admin/adduser", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_ADMIN') OR hasRole('ROLE_APPADMIN')")
    public ResponseEntity<?> addUserDefault(@RequestParam @NotBlank String name,
                                            @RequestParam @Email String email,
                                            @RequestParam (required = false, defaultValue = "default")
                                                    String role,
                                            HttpServletRequest request,
                                            Principal principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        UserRole roleAppAdmin = userRoleService.getUserRole(Role.APPADMIN.getText());
        name = name.trim();
        email = email.trim();

        if (!userService.isEmailAvailable(email)) {
            ErrorRest errorRest = new ErrorRest(HttpStatus.CONFLICT,
                    "user with email: " + email +
                    " has already been registered",
                    "duplicate resource");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorRest);
        } else {
            try {
                User userNew = new User();
                userNew.setName(name);
                userNew.setEmail(email);
                String password = userService.generateRandomPassword(10);
                String activationToken = UUID.randomUUID().toString();
                String appUrl = request.getScheme() + "://" +
                        request.getServerName() + ":" + request.getServerPort(); //development
//				String appUrl = APP_URL;                                         // production
                SimpleMailMessage registrationEmail = new SimpleMailMessage();
                registrationEmail.setFrom("no-replay@domain.com");
                registrationEmail.setTo(userNew.getEmail());
                registrationEmail.setSubject("Registration Confirmation");
                registrationEmail.setText("To confirm your e-mail address, please click the link below:\n" +
                        appUrl + "/api/user/activate?activationToken=" + activationToken +
                        "\n\n Your temporary password is: \n" + password +
                        // obviously not true
                        "\n Please change it immediately after you log in - it is NOT encrypted until then");
                registrationEmail.setFrom("noreply@domain.com");
                emailService.sendEmail(registrationEmail);
                userNew.setPassword(passwordEncoder.encode(password));
                userNew.setEnabled(false);
                userNew.setActivationToken(activationToken);
                switch(role){
                    case "admin":
                        userService.addWithAdminRole(userNew);
                        break;
                    case "appadmin":
                        if(!currentUser.getRoles().contains(roleAppAdmin)){
                            ErrorRest errorRest = new ErrorRest(HttpStatus.UNAUTHORIZED,
                                    "Your priviledges: " + userService.rolesToString2(currentUser) +
                                            " do not allow adding users with APPADMIN rights",
                                    "Insufficient rights for the operation");
                            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorRest);
                        } else {
                            userService.addWithAppadminRole(userNew);
                        }
                        break;
                    default:
                        userService.addWithDefaultRole(userNew);
                }
            } catch (Exception e) {
                e.printStackTrace();
                ErrorRest errorRest = new ErrorRest(HttpStatus.EXPECTATION_FAILED,
                        "creating a new user with the email address: " +
                                email + " failed",
                        "registration failed");
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(errorRest);
            }
        }
        String responseMessage = "new user created, an email with activation link sent to: " +
                email;
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseMessage);
    }


}











