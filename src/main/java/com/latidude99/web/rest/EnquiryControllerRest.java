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

import com.latidude99.model.Comment;
import com.latidude99.model.Enquiry;
import com.latidude99.model.User;
import com.latidude99.service.EmailService;
import com.latidude99.service.EnquiryService;
import com.latidude99.service.UserService;
import com.latidude99.util.EnquiryStatsRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.ZonedDateTime;


@RestController
@RequestMapping("/api")
@Validated
public class EnquiryControllerRest {
    private static final Logger logger = LoggerFactory.getLogger(EnquiryControllerRest.class);
    private final String baseUrl = "http://localhost:8080/"; // development
//	private final String baseUrl = "http://enquiry.latidude99.com/"; // production

    @Autowired
    UserService userService;

    @Autowired
    EnquiryService enquiryService;

    @Autowired
    EmailService emailService;

    @Autowired
    EnquiryStatsRest enquiryStatsRest;

    @GetMapping(path = "/enquiry/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> enquiryPageGoto(@PathVariable Long id) {
        Enquiry enquiryToView = enquiryService.getById(id);
        if (enquiryToView != null) {
            enquiryService.sortProgressUsers(enquiryToView);
        } else {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(enquiryToView);
    }

    @PostMapping(path = "/enquiry/{id}/comment",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> enquiryAddComment(@RequestParam String comment,
                                               @PathVariable int id,
                                               Principal  principal) {
        String userName = principal.getName();
        Enquiry enquiryToUpdate = enquiryService.getById(id);
        if (enquiryToUpdate != null) {
            enquiryToUpdate = enquiryService.saveComment(comment, id, userName);
            enquiryToUpdate = enquiryService.sortProgressUsers(enquiryToUpdate);
            enquiryService.save(enquiryToUpdate);
        } else {
            ErrorRest errorRest = new ErrorRest(HttpStatus.NOT_FOUND,
                    "Enquiry number " + id + " does not exist",
                    "resource not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorRest);
        }
        return ResponseEntity.ok(enquiryToUpdate);
    }


    @PostMapping(path = "/enquiry/{id}/email",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> enquirySendEmail(@RequestParam (required = false) String email,
                                              @PathVariable int id,
                                              Principal  principal) {
        // default: sends email to the current user if no email param set
        if(email == null || "".equals(email)){
           email = principal.getName();
        }
        Enquiry enquiryToEmail = enquiryService.getById(id);
        if (enquiryToEmail != null) {
            enquiryToEmail = enquiryService.sortProgressUsers(enquiryToEmail);
            try {
                emailService.sendSimpleMessage(enquiryToEmail, email);
            } catch (Exception e) {
                ErrorRest errorRest = new ErrorRest(HttpStatus.EXPECTATION_FAILED,
                        "Error ocurred while sending an email to: "
                                + email
                                + " - please check the email address for typos",
                        "Sending the email failed," +
                        " please check your Internet connection and try again");
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(errorRest);
            }
        } else {
            ErrorRest errorRest = new ErrorRest(HttpStatus.NOT_FOUND,
                    "Enquiry number " + id + " does not exist",
                    "resource not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorRest);
        }
        return ResponseEntity.ok(enquiryToEmail);
    }

    @GetMapping(path = "/enquiry/{id}/assign",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> enquiryAssign(@PathVariable int id, Principal  principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        Enquiry enquiryToAssign = enquiryService.getById(id);
        if (enquiryToAssign != null) {
            enquiryToAssign.addProgressUser(currentUser);
            enquiryToAssign.setStatus("in progress");
            enquiryToAssign = enquiryService.sortProgressUsers(enquiryToAssign);

            enquiryService.save(enquiryToAssign);
            enquiryStatsRest.setEnquiry(enquiryToAssign);
            enquiryStatsRest.setWaiting(enquiryService.getNumByStatus("waiting"));
            enquiryStatsRest.setOpened(enquiryService.getNumByStatus("in progress"));
            enquiryStatsRest.setClosed(enquiryService.getNumByStatus("closed"));
            enquiryStatsRest.setAssignedToUser(
                    enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress"));
            enquiryStatsRest.setClosedByUser(
                    enquiryService.getNumByClosingUserAndStatus(currentUser, "closed"));
        } else {
            ErrorRest errorRest = new ErrorRest(HttpStatus.NOT_FOUND,
                    "Enquiry number " + id + " does not exist",
                    "resource not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorRest);
        }
        return ResponseEntity.ok(enquiryStatsRest);
    }

    @GetMapping(path = "/enquiry/{id}/deassign",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> enquiryDeassign(@PathVariable int id, Principal  principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        Enquiry enquiryToDeassign = enquiryService.getById(id);
        if (enquiryToDeassign != null) {
            enquiryToDeassign.removeProgressUser(currentUser);
            if (enquiryToDeassign.getProgressUser().isEmpty()) {
                enquiryToDeassign.setStatus("waiting");
            } else {
                enquiryToDeassign.setStatus("in progress");
            };
            enquiryToDeassign = enquiryService.sortProgressUsers(enquiryToDeassign);
            enquiryService.save(enquiryToDeassign);

            enquiryStatsRest.setEnquiry(enquiryToDeassign);
            enquiryStatsRest.setWaiting(enquiryService.getNumByStatus("waiting"));
            enquiryStatsRest.setOpened(enquiryService.getNumByStatus("in progress"));
            enquiryStatsRest.setClosed(enquiryService.getNumByStatus("closed"));
            enquiryStatsRest.setAssignedToUser(
                    enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress"));
            enquiryStatsRest.setClosedByUser(
                    enquiryService.getNumByClosingUserAndStatus(currentUser, "closed"));
        } else {
            ErrorRest errorRest = new ErrorRest(HttpStatus.NOT_FOUND,
                    "Enquiry number " + id + " does not exist",
                    "resource not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorRest);
        }
        return ResponseEntity.ok(enquiryStatsRest);
    }

    @GetMapping(path = "/enquiry/{id}/close",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> enquiryClose(@PathVariable int id, Principal  principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        Enquiry enquiryToClose = enquiryService.getById(id);
        if (enquiryToClose != null) {
            enquiryToClose.setClosingUser(currentUser);
            enquiryToClose.setClosedDate(ZonedDateTime.now());
            enquiryToClose.setStatus("closed");
            enquiryToClose = enquiryService.sortProgressUsers(enquiryToClose);
            enquiryService.save(enquiryToClose);

            enquiryStatsRest.setEnquiry(enquiryToClose);
            enquiryStatsRest.setWaiting(enquiryService.getNumByStatus("waiting"));
            enquiryStatsRest.setOpened(enquiryService.getNumByStatus("in progress"));
            enquiryStatsRest.setClosed(enquiryService.getNumByStatus("closed"));
            enquiryStatsRest.setAssignedToUser(
                    enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress"));
            enquiryStatsRest.setClosedByUser(
                    enquiryService.getNumByClosingUserAndStatus(currentUser, "closed"));
        } else {
            ErrorRest errorRest = new ErrorRest(HttpStatus.NOT_FOUND,
                    "Enquiry number " + id + " does not exist",
                    "resource not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorRest);
        }
        return ResponseEntity.ok(enquiryStatsRest);
    }

    @GetMapping(path = "/enquiry/{id}/open",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> enquiryOpen(@PathVariable int id, Principal  principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        Enquiry enquiryToOpen = enquiryService.getById(id);
        if (enquiryToOpen != null) {
            enquiryToOpen.setStatus("in progress");
            enquiryToOpen.setClosedDate(null);
            enquiryToOpen.setClosingUser(null);
            enquiryToOpen = enquiryService.sortProgressUsers(enquiryToOpen);
            enquiryService.save(enquiryToOpen);

            enquiryStatsRest.setEnquiry(enquiryToOpen);
            enquiryStatsRest.setWaiting(enquiryService.getNumByStatus("waiting"));
            enquiryStatsRest.setOpened(enquiryService.getNumByStatus("in progress"));
            enquiryStatsRest.setClosed(enquiryService.getNumByStatus("closed"));
            enquiryStatsRest.setAssignedToUser(
                    enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress"));
            enquiryStatsRest.setClosedByUser(
                    enquiryService.getNumByClosingUserAndStatus(currentUser, "closed"));
        } else {
            ErrorRest errorRest = new ErrorRest(HttpStatus.NOT_FOUND,
                    "Enquiry number " + id + " does not exist",
                    "resource not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorRest);
        }
        return ResponseEntity.ok(enquiryStatsRest);
    }

}


