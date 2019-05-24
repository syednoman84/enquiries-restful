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

import java.io.IOException;
import java.security.Principal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.latidude99.model.Attachment;
import com.latidude99.model.Enquiry;
import com.latidude99.model.User;
import com.latidude99.service.EmailService;
import com.latidude99.service.EnquiryService;
import com.latidude99.service.UserService;
import com.latidude99.util.EnquiryListWrapper;
import com.latidude99.util.FormBean;
import com.latidude99.util.PdfCreator;


@Controller
public class EnquiryController {
    private static final Logger logger = LoggerFactory.getLogger(EnquiryController.class);
    private final String baseUrl = "http://localhost:8080/"; // development
//	private final String baseUrl = "http://enquiry.latidude99.com/"; // production

    @Autowired
    UserService userService;

    @Autowired
    EnquiryService enquiryService;

    @Autowired
    EmailService emailService;


    @ModelAttribute("formBean")
    public FormBean createFormBean() {
        FormBean formBean = new FormBean();
        formBean.setSelector("0");
        return formBean;
    }


    @GetMapping("/enquiry/form/uploadfail")
    public String uploadFail(RedirectAttributes redirect) {
        redirect.addFlashAttribute("uploadFail", "File upload failed, " +
                "at least one of the attachments is larger than the limit (1024KB/1MB).");
        return "redirect:/enquiry/form";
    }

    @GetMapping("/enquiry/form")
    public String enquiryForm(@ModelAttribute String uploadFail, Model model) {
        Enquiry enquiry = new Enquiry();
        model.addAttribute("enquiry", enquiry);
        model.addAttribute("user", new User());
        model.addAttribute("uploadFail", "File upload failed, " +
                "at least one of the attachments is larger than the limit (1024KB/1MB).");
        return "enquiryForm";
    }

    /*
     * Processes customer enquiry and saves it to the database
     */
    @PostMapping("/enquiry/form")
    public String enquiryAdd(@ModelAttribute @Valid Enquiry enquiry, BindingResult result,
                             @RequestParam MultipartFile[] files, Model model) {
        model.addAttribute("user", new User());
        if (result.hasErrors()) {
            List<FieldError> errors = result.getFieldErrors();
            for (FieldError error : errors) {
                System.out.println(error.getObjectName() + " - " + error.getDefaultMessage());
            }
            return "enquiryForm";
        } else {
            Enquiry enquiryToSave = new Enquiry();
            enquiryToSave.setName(enquiry.getName());
            enquiryToSave.setEmail(enquiry.getEmail());
            if (enquiry.getPhone() != null) enquiryToSave.setPhone(enquiry.getPhone());
            enquiryToSave.setType(enquiry.getType());
            enquiryToSave.setMessage(enquiry.getMessage());
            enquiryToSave.setPolygon(
                    enquiryService.convertRoundBracketToSquareCoordsArrayString(enquiry.getPolygon()));
            enquiryToSave.setPolygonEncoded(enquiry.getPolygonEncoded());
            enquiryToSave.setCreatedDate(ZonedDateTime.now());
            enquiryToSave.setStatus("waiting");
            logger.info("Files length: " + files.length);
            try {
                if (files != null && files.length > 0) {
                    int filesNumber = files.length;
                    for (int i = 0; i < filesNumber; i++) {
                        System.out.println("file number: " + i);
                        if (files[i] != null && files[i].getSize() > 0) {
                            Attachment attachment = new Attachment();
                            attachment.setName(files[i].getOriginalFilename());
                            attachment.setSize(files[i].getSize() / 1024); // in KB
                            attachment.setMimetype(files[i].getContentType());
                            attachment.setEnquiry(enquiryToSave);
                            attachment.setFile(files[i].getBytes());
                            enquiryToSave.addAttachment(attachment);
                        }
                    }
                }
            } catch (IOException e) {
                model.addAttribute("uploadError",
                        "00ps! Something went wrong, try again");
                model.addAttribute("uploadErrorMessage", e.getMessage());
                return "enquirySubmit";
            }
            try {
                byte[] imageByteArray = enquiryService.imageUrlToByteArray(enquiry.getPolygonEncoded());
                enquiryToSave.setImage(imageByteArray);
            } catch (IOException e) {
                logger.error("Error saving static Google Map Polygon image, " +
                        e.getMessage() + ", " + e.getCause() + ", " + e.getLocalizedMessage());

            }
            enquiryService.save(enquiryToSave);
            Enquiry enquiryCheck = enquiryService.getById(enquiryToSave.getId());
            if (enquiryCheck.getAttachments() != null)
                logger.info("Attachments number: " + enquiryCheck.getAttachments().size());
        }
        logger.info("polygon round brackets: " + enquiry.getPolygon());
        logger.info("polygon square brackets: " +
                enquiryService.convertRoundBracketToSquareCoordsArrayString(enquiry.getPolygon()));
        for (MultipartFile file : files) {
            System.err.println(file.getOriginalFilename());
        }
        return "enquirySubmit";
    }

    /*
     * Displays detailed enquiry view from database
     */
    @PostMapping("/enquiry/page")
    public String enquiryPage(@ModelAttribute Enquiry enquiry, Model model, Principal principal
                             /* HttpServletResponse response */) {
        System.out.println("test");
        User currentUser = userService.getUserByUsername(principal.getName());
        model.addAttribute("currentUser", currentUser);
        Long waiting = enquiryService.getNumByStatus("waiting");
        model.addAttribute("waiting", waiting);
        Long opened = enquiryService.getNumByStatus("in progress");
        model.addAttribute("opened", opened);
        Long closed = enquiryService.getNumByStatus("closed");
        model.addAttribute("closed", closed);
        Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
        model.addAttribute("openedByUser", openedByUser);
        int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
        model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
        Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
        model.addAttribute("closedByUser", closedByUser);
        Enquiry enquiryToView = enquiryService.getById(enquiry.getId());
        enquiryService.sortProgressUsers(enquiryToView);
        model.addAttribute("enquiry", enquiryToView);
        model.addAttribute("imageDbUrl", baseUrl + "image/" + enquiryToView.getId());
        model.addAttribute("email", null);
        model.addAttribute("emailFail", null);
        logger.info("imageDbUrl: " + baseUrl + "image/" + enquiryToView.getId());
        return "enquiryPage";
    }

    /*
     * Processes comments added under individual enquiries
     */
    @PostMapping("/enquiry/comment")
    public String enquiryComment(@ModelAttribute FormBean formBean, Model model, Principal principal,
                                 HttpServletResponse response, FormBean fromBean) {
        User currentUser = userService.getUserByUsername(principal.getName());
        model.addAttribute("currentUser", currentUser);
        Long waiting = enquiryService.getNumByStatus("waiting");
        model.addAttribute("waiting", waiting);
        Long opened = enquiryService.getNumByStatus("in progress");
        model.addAttribute("opened", opened);
        Long closed = enquiryService.getNumByStatus("closed");
        model.addAttribute("closed", closed);
        Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
        model.addAttribute("openedByUser", openedByUser);
        int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
        model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
        Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
        model.addAttribute("closedByUser", closedByUser);
        Enquiry enquiryToView = enquiryService.saveComment(fromBean);
        enquiryService.sortProgressUsers(enquiryToView);
        model.addAttribute("enquiry", enquiryToView);
        model.addAttribute("imageDbUrl", baseUrl + "image/" + enquiryToView.getId());
        model.addAttribute("email", null);
        model.addAttribute("emailFail", null);
        return "enquiryPage";
    }

    /*
     * Deals with sending an email with individual enquiries
     */
    @PostMapping("/enquiry/email")
    public String enquiryEmail(@ModelAttribute Enquiry enquiry, Model model, Principal principal,
                               HttpServletResponse response) {
        User currentUser = userService.getUserByUsername(principal.getName());
        model.addAttribute("currentUser", currentUser);
        Long waiting = enquiryService.getNumByStatus("waiting");
        model.addAttribute("waiting", waiting);
        Long opened = enquiryService.getNumByStatus("in progress");
        model.addAttribute("opened", opened);
        Long closed = enquiryService.getNumByStatus("closed");
        model.addAttribute("closed", closed);
        Long openedByUser =
                enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
        model.addAttribute("openedByUser", openedByUser);
        int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
        model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
        Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
        model.addAttribute("closedByUser", closedByUser);
        Enquiry enquiryToView = enquiryService.getById(enquiry.getId());
        enquiryService.sortProgressUsers(enquiryToView);
        model.addAttribute("enquiry", enquiryToView);
        model.addAttribute("imageDbUrl",
                baseUrl + "image/" + enquiryToView.getId());
        try {
            emailService.sendSimpleMessage(enquiryToView, currentUser);
            model.addAttribute("email",
                    "The enquiry has been successfully emailed to: ");
        } catch (Exception e) {
            model.addAttribute("emailFail","Sending the email failed," +
                    " please check your Internet connection and try again");
        }
        return "enquiryPage";
    }

    /*
     * Displays an enquiry with entered number
     */
    @PostMapping("/enquiry/page/goto")
    public String enquiryPageGoto(@ModelAttribute FormBean formBean, Model model, Principal principal,
                                  HttpServletResponse response) {
        User currentUser = userService.getUserByUsername(principal.getName());
        model.addAttribute("currentUser", currentUser);
        Long waiting = enquiryService.getNumByStatus("waiting");
        model.addAttribute("waiting", waiting);
        Long opened = enquiryService.getNumByStatus("in progress");
        model.addAttribute("opened", opened);
        Long closed = enquiryService.getNumByStatus("closed");
        model.addAttribute("closed", closed);
        Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
        model.addAttribute("openedByUser", openedByUser);
        int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
        model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
        Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
        model.addAttribute("closedByUser", closedByUser);
        Enquiry enquiryToView = enquiryService.getById(formBean.getNumber());
        if (enquiryToView != null) {
            enquiryService.sortProgressUsers(enquiryToView);
        } else {
            enquiryToView = new Enquiry();
            enquiryToView.setId(formBean.getNumber());
            enquiryToView.setType("---------------  THERE IS NO ENQUIRY NUMBER: " +
                    formBean.getNumber() + "  ---------------");
        }
        model.addAttribute("enquiry", enquiryToView);
        model.addAttribute("email", null);
        model.addAttribute("emailFail", null);
        return "enquiryPage";
    }

    @PostMapping("/enquiry/page/next")
    public String enquiryPageNext(@ModelAttribute Enquiry enquiry, Model model, Principal principal,
                                  HttpServletResponse response) {
        User currentUser = userService.getUserByUsername(principal.getName());
        model.addAttribute("currentUser", currentUser);
        Long waiting = enquiryService.getNumByStatus("waiting");
        model.addAttribute("waiting", waiting);
        Long opened = enquiryService.getNumByStatus("in progress");
        model.addAttribute("opened", opened);
        Long closed = enquiryService.getNumByStatus("closed");
        model.addAttribute("closed", closed);
        Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
        model.addAttribute("openedByUser", openedByUser);
        int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
        model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
        Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
        model.addAttribute("closedByUser", closedByUser);
        Enquiry enquiryToView = enquiryService.getById(enquiry.getId() + 1);
        if (enquiryToView != null) {
            enquiryService.sortProgressUsers(enquiryToView);
        } else {
            enquiryToView = new Enquiry();
            enquiryToView.setId(enquiry.getId() + 1);
            enquiryToView.setType("---------------  THERE IS NO ENQUIRY NUMBER: " +
                    (enquiry.getId() + 1) + "  ---------------");
        }
        model.addAttribute("enquiry", enquiryToView);
        model.addAttribute("email", null);
        model.addAttribute("emailFail", null);
        return "enquiryPage";
    }

    @PostMapping("/enquiry/page/previous")
    public String enquiryPagePrevious(@ModelAttribute Enquiry enquiry, Model model, Principal principal,
                                      HttpServletResponse response) {
        User currentUser = userService.getUserByUsername(principal.getName());
        model.addAttribute("currentUser", currentUser);
        Long waiting = enquiryService.getNumByStatus("waiting");
        model.addAttribute("waiting", waiting);
        Long opened = enquiryService.getNumByStatus("in progress");
        model.addAttribute("opened", opened);
        Long closed = enquiryService.getNumByStatus("closed");
        model.addAttribute("closed", closed);
        Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
        model.addAttribute("openedByUser", openedByUser);
        int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
        model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
        Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
        model.addAttribute("closedByUser", closedByUser);
        Enquiry enquiryToView = enquiryService.getById(enquiry.getId() - 1);
        if (enquiryToView != null) {
            enquiryService.sortProgressUsers(enquiryToView);
        } else {
            enquiryToView = new Enquiry();
            enquiryToView.setId(enquiry.getId() - 1);
            enquiryToView.setType("---------------  THERE IS NO ENQUIRY NUMBER: " +
                    (enquiry.getId() - 1) + "  ---------------");
        }
        model.addAttribute("enquiry", enquiryToView);
        model.addAttribute("email", null);
        model.addAttribute("emailFail", null);
        return "enquiryPage";
    }

    /*
     * Assigns the displayed enquiry to the current logged in user
     */
    @PostMapping("/enquiry/assign")
    public String assign(@ModelAttribute Enquiry enquiry, Model model, Principal principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        model.addAttribute("currentUser", currentUser);
        Enquiry enquiryToView = enquiryService.getById(enquiry.getId());
        enquiryToView.addProgressUser(currentUser);
        enquiryToView.setStatus("in progress");
        enquiryService.save(enquiryToView);
        enquiryService.sortProgressUsers(enquiryToView);
        Long waiting = enquiryService.getNumByStatus("waiting");
        model.addAttribute("waiting", waiting);
        Long opened = enquiryService.getNumByStatus("in progress");
        model.addAttribute("opened", opened);
        Long closed = enquiryService.getNumByStatus("closed");
        model.addAttribute("closed", closed);
        Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
        model.addAttribute("openedByUser", openedByUser);
        int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
        model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
        Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
        model.addAttribute("closedByUser", closedByUser);
        model.addAttribute("enquiry", enquiryToView);
        model.addAttribute("email", null);
        model.addAttribute("emailFail", null);
        return "enquiryPage";
    }

    /*
     * Removes the assignment from the current logged in user (if there was one)
     */
    @PostMapping("/enquiry/deassign")
    public String deassign(@ModelAttribute Enquiry enquiry, Model model, Principal principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        model.addAttribute("currentUser", currentUser);
        Enquiry enquiryToView = enquiryService.getById(enquiry.getId());
        enquiryToView.removeProgressUser(currentUser);
        if (enquiryToView.getProgressUser().isEmpty()) {
            enquiryToView.setStatus("waiting");
        } else {
            enquiryToView.setStatus("in progress");
        }
        enquiryService.save(enquiryToView);
        enquiryService.sortProgressUsers(enquiryToView);
        Long waiting = enquiryService.getNumByStatus("waiting");
        model.addAttribute("waiting", waiting);
        Long opened = enquiryService.getNumByStatus("in progress");
        model.addAttribute("opened", opened);
        Long closed = enquiryService.getNumByStatus("closed");
        model.addAttribute("closed", closed);
        Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
        model.addAttribute("openedByUser", openedByUser);
        int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
        model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
        Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
        model.addAttribute("closedByUser", closedByUser);
        model.addAttribute("enquiry", enquiryToView);
        model.addAttribute("email", null);
        model.addAttribute("emailFail", null);
        return "enquiryPage";
    }

    /*
     * Closes the displayed enquiry and attributes that to the logged in user
     */
    @PostMapping("/enquiry/close")
    public String close(@ModelAttribute Enquiry enquiry, Model model, Principal principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        model.addAttribute("currentUser", currentUser);
        Enquiry enquiryToView = enquiryService.getById(enquiry.getId());
        enquiryToView.setClosingUser(currentUser);
        enquiryToView.setClosedDate(ZonedDateTime.now());
        enquiryToView.setStatus("closed");
        enquiryService.save(enquiryToView);
        enquiryService.sortProgressUsers(enquiryToView);
        Long waiting = enquiryService.getNumByStatus("waiting");
        model.addAttribute("waiting", waiting);
        Long opened = enquiryService.getNumByStatus("in progress");
        model.addAttribute("opened", opened);
        Long closed = enquiryService.getNumByStatus("closed");
        model.addAttribute("closed", closed);
        Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
        model.addAttribute("openedByUser", openedByUser);
        int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
        model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
        Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
        model.addAttribute("closedByUser", closedByUser);
        model.addAttribute("enquiry", enquiryToView);
        model.addAttribute("email", null);
        model.addAttribute("emailFail", null);
        return "enquiryPage";
    }

    /*
     * Opens the displayed enquiry (if closed)
     */
    @PostMapping("/enquiry/open")
    public String open(@ModelAttribute Enquiry enquiry, Model model, Principal principal) {
        User currentUser = userService.getUserByUsername(principal.getName());
        model.addAttribute("currentUser", currentUser);
        Enquiry enquiryToView = enquiryService.getById(enquiry.getId());
        enquiryToView.setStatus("in progress");
        enquiryToView.setClosedDate(null);
        enquiryService.save(enquiryToView);
        enquiryService.sortProgressUsers(enquiryToView);
        Long waiting = enquiryService.getNumByStatus("waiting");
        model.addAttribute("waiting", waiting);
        Long opened = enquiryService.getNumByStatus("in progress");
        model.addAttribute("opened", opened);
        Long closed = enquiryService.getNumByStatus("closed");
        model.addAttribute("closed", closed);
        Long openedByUser = enquiryService.getNumByProgressUserAndStatus(currentUser, "in progress");
        model.addAttribute("openedByUser", openedByUser);
        int assignedToUserAndClosed = enquiryService.getNumByClosedAndUserAssigned(currentUser);
        model.addAttribute("assignedToUserAndClosed", assignedToUserAndClosed);
        Long closedByUser = enquiryService.getNumByClosingUserAndStatus(currentUser, "closed");
        model.addAttribute("closedByUser", closedByUser);
        model.addAttribute("enquiry", enquiryToView);
        model.addAttribute("email", null);
        model.addAttribute("emailFail", null);
        return "enquiryPage";
    }


}






