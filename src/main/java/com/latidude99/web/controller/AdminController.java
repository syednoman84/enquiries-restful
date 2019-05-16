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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.RandomStringUtils;
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
import org.thymeleaf.standard.expression.GenericTokenExpression;

import com.latidude99.model.Role;
import com.latidude99.model.User;
import com.latidude99.model.UserRole;
import com.latidude99.service.EmailService;
import com.latidude99.service.EnquiryService;
import com.latidude99.service.UserRoleService;
import com.latidude99.service.UserService;

@Controller
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
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


    @GetMapping("/admin")
    public String admin(Model model, Principal principal) {
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
        List<User> users = userService.getAllSinCurrent(currentUser);
        model.addAttribute("users", users);
        model.addAttribute("emailResetOK", null);
        model.addAttribute("emailActivationOK", null);
        model.addAttribute("emailResetError", null);
        model.addAttribute("admin", null);
        model.addAttribute("privileges", null);
        return "enquiryAdmin";
    }

    /*
     * Allows admin users to change other users/admins privileges
     */
    @PostMapping("/user/privileges")
    public String privileges(@ModelAttribute User user, Model model, Principal principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        User userToUpdate = userService.findById(user.getId());
        model.addAttribute("privileges", null);

        UserRole roleUser = userRoleService.getUserRole(Role.DEFAULT.getText());
        UserRole roleAdmin = userRoleService.getUserRole(Role.ADMIN.getText());
        UserRole roleAppAdmin = userRoleService.getUserRole(Role.APPADMIN.getText());
        if (currentUser.getRoles().contains(roleAppAdmin) &&
                !userToUpdate.getRoles().contains(roleAppAdmin)) {
            if (userToUpdate.getRoles().contains(roleUser)) {
                Set<UserRole> roles = new HashSet<>();
                roles.add(roleAdmin);
                userToUpdate.setRoles(roles);
            } else {
                Set<UserRole> roles = new HashSet<>();
                roles.add(roleUser);
                userToUpdate.setRoles(roles);
            }
        } else if (currentUser.getRoles().contains(roleAdmin) &&
                !userToUpdate.getRoles().contains(roleAppAdmin)) {
            if (userToUpdate.getRoles().contains(roleUser)) {
                Set<UserRole> roles = new HashSet<>();
                roles.add(roleAdmin);
                userToUpdate.setRoles(roles);
            } else {
                Set<UserRole> roles = new HashSet<>();
                roles.add(roleUser);
                userToUpdate.setRoles(roles);
            }
        } else if (currentUser.getRoles().contains(roleAdmin) &&
                userToUpdate.getRoles().contains(roleAppAdmin)) {
            model.addAttribute("privileges",
                    "Insufficient privileges for the operation");
        }
        userService.save(userToUpdate);
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
        List<User> users = userService.getAllSinCurrent(currentUser);
        model.addAttribute("users", users);
        return "enquiryAdmin";
    }

    /*
     * Allows admins to block/unblock users
     */
    @PostMapping("/user/block")
    public String block(@ModelAttribute User user, Model model, Principal principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        User userToUpdate = userService.findById(user.getId());

        model.addAttribute("privileges", null);
        UserRole roleAdmin = userRoleService.getUserRole(Role.ADMIN.getText());
        UserRole roleAppAdmin = userRoleService.getUserRole(Role.APPADMIN.getText());
        if (currentUser.getRoles().contains(roleAdmin) ||
                !currentUser.getRoles().contains(roleAppAdmin)) {
            if (userToUpdate.getRoles().contains(roleAppAdmin))
                model.addAttribute("privileges",
                        "Insufficient privileges for the operation");
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
        }
        if (currentUser.getRoles().contains(roleAppAdmin)) {
            if (!userToUpdate.getRoles().contains(roleAppAdmin) && userToUpdate.isBlocked()) {
                userToUpdate.setBlocked(false);
                userService.save(userToUpdate);
            } else if (!userToUpdate.getRoles().contains(roleAppAdmin) && !userToUpdate.isBlocked()) {
                userToUpdate.setBlocked(true);
                userService.save(userToUpdate);
            }
        }
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
        List<User> users = userService.getAllSinCurrent(currentUser);
        model.addAttribute("users", users);
        return "enquiryAdmin";
    }

    /*
     * Allows admins to activate/deactivate users
     */
    @PostMapping("/user/enable")
    public String enable(@ModelAttribute User user, Model model, Principal principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        User userToUpdate = userService.findById(user.getId());

        model.addAttribute("privileges", null);
        UserRole roleAdmin = userRoleService.getUserRole(Role.ADMIN.getText());
        UserRole roleAppAdmin = userRoleService.getUserRole(Role.APPADMIN.getText());

        if (currentUser.getRoles().contains(roleAdmin) ||
                !currentUser.getRoles().contains(roleAppAdmin)) {
            if (userToUpdate.getRoles().contains(roleAppAdmin))
                model.addAttribute("privileges",
                        "Insufficient privileges for the operation");
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
        }
        if (currentUser.getRoles().contains(roleAppAdmin)) {
            if (!userToUpdate.getRoles().contains(roleAppAdmin) &&
                    userToUpdate.isEnabled()) {
                userToUpdate.setEnabled(false);
                userService.save(userToUpdate);
            } else if (!userToUpdate.getRoles().contains(roleAppAdmin) &&
                    !userToUpdate.isEnabled()) {
                userToUpdate.setEnabled(true);
                userService.save(userToUpdate);
            }
        }
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
        List<User> users = userService.getAllSinCurrent(currentUser);
        model.addAttribute("users", users);

        return "enquiryAdmin";
    }

    /*
     * Allows admins to reset users' passwords and send a password reset link
     */
    @PostMapping("/user/reset")
    public String resetEmail(@ModelAttribute User user, Model model,
                             HttpServletRequest request, Principal principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        User userToReset = userService.findById(user.getId());
        model.addAttribute("currentUser", currentUser);
        Long waiting = enquiryService.getNumByStatus("waiting");
        model.addAttribute("waiting", waiting);
        Long opened = enquiryService.getNumByStatus("in progress");
        model.addAttribute("opened", opened);
        Long closed = enquiryService.getNumByStatus("closed");
        model.addAttribute("closed", closed);
        Long openedByUser =
                enquiryService.getNumByProgressUserAndStatus(currentUser, "opened");
        model.addAttribute("openedByUser", openedByUser);
        Long closedByUser =
                enquiryService.getNumByClosingUserAndStatus(currentUser, "opened");
        model.addAttribute("closedByUser", closedByUser);
        List<User> users = userService.getAllSinCurrent(currentUser);
        model.addAttribute("users", users);
        model.addAttribute("privileges", null);

        UserRole roleAdmin = userRoleService.getUserRole(Role.ADMIN.getText());
        UserRole roleAppAdmin = userRoleService.getUserRole(Role.APPADMIN.getText());

        if (request.isUserInRole(Role.ADMIN.getText())) {
            if (!userToReset.getRoles().contains(roleAdmin) &&
                    !userToReset.getRoles().contains(roleAppAdmin) &&
                    userToReset.getEmail() != null) {
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
                            appUrl + "/user/reset?resetToken=" + resetToken);
                    resetEmail.setFrom("noreply@domain.com");
                    emailService.sendEmail(resetEmail);
                    userToReset.setResetToken(resetToken);
                    userService.save(userToReset);
                    model.addAttribute("emailResetError", null);
                    model.addAttribute("emailResetOK", userToReset);
                    logger.info("email sent to: " + userToReset.getEmail());
                } catch (Exception e) {
                    model.addAttribute("emailResetError", "error");
                    logger.info("sending email failed");
                    return "enquiryAdmin";
                } finally {
                    model.addAttribute("admin", null);
                }
            } else {
                model.addAttribute("admin", "admin");
                logger.info("user's email not found");
                return "enquiryAdmin";
            }
        } else if (request.isUserInRole(Role.APPADMIN.getText())) {
            if (!userToReset.getRoles().contains(roleAppAdmin) && userToReset.getEmail() != null) {
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
                            appUrl + "/user/reset?resetToken=" + resetToken);
                    resetEmail.setFrom("noreply@domain.com");
                    emailService.sendEmail(resetEmail);
                    userToReset.setResetToken(resetToken);
                    userService.save(userToReset);
                    model.addAttribute("emailResetError", null);
                    model.addAttribute("emailResetOK", userToReset);
                    logger.info("email sent to: " + userToReset.getEmail());
                } catch (Exception e) {
                    model.addAttribute("emailResetError", "error");
                    logger.info("sending email failed");
                    return "enquiryAdmin";
                } finally {
                    model.addAttribute("admin", null);
                }
            } else {
                model.addAttribute("admin", "admin");
                logger.info("user's email not found");
                return "enquiryAdmin";
            }
        }
        return "enquiryAdmin";
    }

    /*
     * Allows admins to re-send an activation link
     */
    @PostMapping("/user/activate")
    public String activationEmail(@ModelAttribute User user, Model model,
                                  HttpServletRequest request, Principal principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        User userToActivate = userService.findById(user.getId());
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
        List<User> users = userService.getAllSinCurrent(currentUser);
        model.addAttribute("users", users);

        model.addAttribute("privileges", null);
        UserRole roleAdmin = userRoleService.getUserRole(Role.ADMIN.getText());
        UserRole roleAppAdmin = userRoleService.getUserRole(Role.APPADMIN.getText());
        if (request.isUserInRole(Role.ADMIN.getText())) {
            if (!userToActivate.getRoles().contains(roleAdmin) &&
                    !userToActivate.getRoles().contains(roleAppAdmin) &&
                    userToActivate.getEmail() != null) {
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
                            appUrl + "/user/activate?activationToken=" + activationToken);
                    activationEmail.setFrom("noreply@domain.com");
                    emailService.sendEmail(activationEmail);
                    userToActivate.setActivationToken(activationToken);
                    userService.save(userToActivate);
                    model.addAttribute("emailResetError", null);
                    model.addAttribute("emailActivationOK", userToActivate);
                    logger.info("email sent to: " + userToActivate.getEmail());
                } catch (Exception e) {
                    model.addAttribute("emailActivationError", "error");
                    logger.info("sending email failed");
                    return "enquiryAdmin";
                } finally {
                    model.addAttribute("noemail", null);
                }
            } else {
                model.addAttribute("admin", "admin");
                logger.info("user's email not found");
                return "enquiryAdmin";
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
                        appUrl + "/user/activate?activationToken=" + activationToken);
                activationEmail.setFrom("noreply@domain.com");
                emailService.sendEmail(activationEmail);
                userToActivate.setActivationToken(activationToken);
                userService.save(userToActivate);
                model.addAttribute("emailResetError", null);
                model.addAttribute("emailActivationOK", userToActivate);
                logger.info("email sent to: " + userToActivate.getEmail());
            } catch (Exception e) {
                model.addAttribute("emailActivationError", "error");
                logger.info("sending email failed");
                return "enquiryAdmin";
            } finally {
                model.addAttribute("noemail", null);
            }
        } else {
            model.addAttribute("admin", "admin");
            logger.info("user's email not found");
            return "enquiryAdmin";
        }
        return "enquiryAdmin";
    }

    /*
     * Displays form for adding new users/admins
     */
    @GetMapping("/user/add")
    public String addUser(Model model, Principal principal) {
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
        User userNew = new User();
        userNew.setPassword("not_important");
        model.addAttribute("userNew", userNew);
        model.addAttribute("addDefaultOK", null);
        model.addAttribute("addAdminOK", null);
        model.addAttribute("addError", null);
        model.addAttribute("nameTaken", null);
        model.addAttribute("emailTaken", null);
        return "addUser";
    }

    /*
     * Processes adding nwe users with USER priviledges
     */
    @PostMapping("/user/add/defaultRole")
    public String addUserDefault(@ModelAttribute @Valid User userNew, BindingResult bindResult,
                                 HttpServletRequest request, Model model, Principal principal) {
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
        model.addAttribute("userNew", userNew);
        model.addAttribute("addAdminOK", null);
        if (bindResult.hasErrors()) {
            return "addUser";
        } else if (!userService.isNameAvailable(userNew)) {
            model.addAttribute("nameTaken", userNew.getName());
            return "addUser";
        } else if (!userService.isEmailAvailable(userNew)) {
            model.addAttribute("emailTaken", userNew.getEmail());
            return "addUser";
        } else {
            try {
                String password = generateRandomPassword(10);
                String activationToken = UUID.randomUUID().toString();
                String appUrl = request.getScheme() + "://" +
                        request.getServerName() + ":" + request.getServerPort(); //development
//				String appUrl = APP_URL;                                         // production
                SimpleMailMessage registrationEmail = new SimpleMailMessage();
                registrationEmail.setFrom("no-replay@domain.com");
                registrationEmail.setTo(userNew.getEmail());
                registrationEmail.setSubject("Registration Confirmation");
                registrationEmail.setText("To confirm your e-mail address, please click the link below:\n" +
                        appUrl + "/user/activate?activationToken=" + activationToken +
                        "\n\n Your temporary password is: \n" + password +
                        "\n Please change it immediately after you log in - it is NOT encrypted until then");
                registrationEmail.setFrom("noreply@domain.com");
                emailService.sendEmail(registrationEmail);
                userNew.setName(userNew.getName().trim());
                userNew.setPassword(passwordEncoder.encode(password));
                userNew.setEnabled(false);
                userNew.setActivationToken(activationToken);
                userService.addWithDefaultRole(userNew);
                model.addAttribute("addDefaultOK", userNew);
                model.addAttribute("addError", null);
                model.addAttribute("nameTaken", null);
                model.addAttribute("emailTaken", null);
                logger.info("Activation link sent, password : " + password);
                User userNewAnother = new User();
                userNewAnother.setPassword("not_important");
                model.addAttribute("userNew", userNewAnother);
                return "addUser";
            } catch (Exception e) {
                model.addAttribute("addError", "error");
                return "addUser";
            }
        }
    }

    /*
     * Processes adding nwe users with ADMIN priviledges
     */
    @PostMapping("/user/add/adminRole")
    public String addUserAdmin(@ModelAttribute @Valid User userNew, BindingResult bindResult,
                               HttpServletRequest request, Model model, Principal principal) {
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
        model.addAttribute("userNew", userNew);
        model.addAttribute("addDefaultOK", null);
        if (bindResult.hasErrors()) {
            return "addUser";
        } else if (!userService.isNameAvailable(userNew)) {
            model.addAttribute("nameTaken", userNew.getName());
            return "addUser";
        } else if (!userService.isEmailAvailable(userNew)) {
            model.addAttribute("emailTaken", userNew.getEmail());
            return "addUser";
        } else {
            try {
                String password = generateRandomPassword(10);
                String activationToken = UUID.randomUUID().toString();
                String appUrl = request.getScheme() + "://" +
                        request.getServerName() + ":" + request.getServerPort(); //development
//				String appUrl = APP_URL;                                        // production
                SimpleMailMessage registrationEmail = new SimpleMailMessage();
                registrationEmail.setFrom("no-replay@domain.com");
                registrationEmail.setTo(userNew.getEmail());
                registrationEmail.setSubject("Registration Confirmation");
                registrationEmail.setText("To confirm your e-mail address, please click the link below:\n" +
                        appUrl + "/user/activate?activationToken=" + activationToken +
                        "\n\n Your temporary password is: \n" + password +
                        "\n Please change it immediately after you log in - it is NOT encrypted until then");
                registrationEmail.setFrom("noreply@domain.com");
                emailService.sendEmail(registrationEmail);
                userNew.setName(userNew.getName().trim());
                userNew.setPassword(passwordEncoder.encode(password));
                userNew.setEnabled(false);
                userNew.setActivationToken(activationToken);
                userService.addWithAdminRole(userNew);
                model.addAttribute("addAdminOK", userNew);
                model.addAttribute("addError", null);
                model.addAttribute("nameTaken", null);
                model.addAttribute("emailTaken", null);
                logger.info("Activation link sent, password : " + password);
                User userNewAnother = new User();
                userNew.setPassword("not_important");
                model.addAttribute("userNew", userNewAnother);
                return "addUser";
            } catch (Exception e) {
                model.addAttribute("addError", "error");
                return "addUser";
            }
        }
    }

    public String generateRandomPassword(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"; //~`!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?";
        String password = RandomStringUtils.random(length, characters);
        return password;
    }

}
  



	





























