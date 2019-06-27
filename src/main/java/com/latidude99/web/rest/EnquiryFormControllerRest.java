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

import com.latidude99.model.Attachment;
import com.latidude99.model.Enquiry;
import com.latidude99.service.EnquiryService;
import com.latidude99.service.UserService;
import com.latidude99.util.EnquiryFormWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.net.URI;
import java.time.ZonedDateTime;


@RestController
@RequestMapping("/api")
@Validated
public class EnquiryFormControllerRest {
    private static final Logger logger = LoggerFactory.getLogger(EnquiryFormControllerRest.class);
    private final String baseUrl = "http://localhost:8080/"; // development
//	private final String baseUrl = "http://enquiry.latidude99.com/"; // production

    @Autowired
    UserService userService;

    @Autowired
    EnquiryService enquiryService;

    @GetMapping(path = "/enquiry/form", produces = MediaType.APPLICATION_JSON_VALUE)
    public Enquiry enquiryForm() {
        Enquiry enquiry = new Enquiry();
        return enquiry;
    }
    /*
     * Saves the enquiry in DB
     * Polygon is expected as number of vertexes comma separated and
     * surrounded with parentheses '()', which are then converted into square brackets '[]'
     * and additional pair of '[]' is added around all of them (making it a format ready
     * to display using Google Maps API v3 on the user side), then they are saved in DB
     */
    @PostMapping(path = "/enquiry/form",
                consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> enquiryAdd(@RequestParam @NotBlank @Size(min=3) String name,
                                        @RequestParam @NotBlank @Email String email,
                                        @RequestParam String phone,
                                        @RequestParam String isbn,
                                        @RequestParam @NotBlank String type,
                                        @RequestParam @NotBlank @Size(min=10, max = 20148) String message,
                                        @RequestParam String polygon,
                                        @RequestParam String polygonencoded,
                                        @RequestParam MultipartFile[] files) {
        Enquiry enquiryToSave = new Enquiry();
        enquiryToSave.setName(name);
        enquiryToSave.setEmail(email);
        enquiryToSave.setType(type);
        if (phone != null) enquiryToSave.setPhone(phone);
        if (isbn != null) enquiryToSave.setIsbn(isbn);
        enquiryToSave.setMessage(message);
        enquiryToSave.setPolygon(enquiryService.convertRoundBracketToSquareCoordsArrayString(polygon));
        enquiryToSave.setPolygonEncoded(polygonencoded);
        enquiryToSave.setCreatedDate(ZonedDateTime.now());
        enquiryToSave.setStatus("waiting");

        EnquiryFormWrapper enquiryFormWrapper = new EnquiryFormWrapper(enquiryToSave);
        try {
            if (files != null && files.length > 0) {
                int filesNumber = files.length;
                for (int i = 0; i < filesNumber; i++) {
                    if (files[i] != null && files[i].getSize() > 0) {
                        Attachment attachment = new Attachment();
                        attachment.setName(files[i].getOriginalFilename());
                        attachment.setSize(files[i].getSize() / 1024); // in KB
                        attachment.setMimetype(files[i].getContentType());
                        attachment.setEnquiry(enquiryToSave);
                        attachment.setFile(files[i].getBytes());
                        enquiryToSave.addAttachment(attachment);
                        enquiryFormWrapper.getFormReturnInfo().put(
                                "attachment " + (i+1) + " name: " + attachment.getName(),
                                "size: " + attachment.getSize() + " bytes");
                        enquiryFormWrapper.setAttachmentsNumber(enquiryFormWrapper.getAttachmentsNumber() + 1);
                    }
                }
            }
        } catch (IOException e) {
            enquiryFormWrapper.getFormReturnInfo().put("attachment error: ",
                   e.getMessage() + ",/r/n " + e.getCause() + ",/r/n " + e.getLocalizedMessage());
        }
        // saves polygon rendered onto GoogleMaps as static image
        try {
            byte[] imageByteArray = enquiryService.imageUrlToByteArray(polygonencoded);
            enquiryToSave.setImage(imageByteArray);
            enquiryFormWrapper.getFormReturnInfo().put(
                    "polygon image saved: ",
                    "size: " + imageByteArray.length + " bytes");
        } catch (IOException e) {
            enquiryFormWrapper.getFormReturnInfo().put("polygon image error: ",
                    e.getMessage() + ",/r/n " + e.getCause() + ",/r/n " + e.getLocalizedMessage());
        }
        enquiryService.save(enquiryToSave);

        URI location = ServletUriComponentsBuilder
                .fromPath("/api/enquiry")
                .path("/{id}")
                .buildAndExpand(enquiryFormWrapper.getId())
                .toUri();
        return ResponseEntity.created(location).body(enquiryFormWrapper);

    }


}


