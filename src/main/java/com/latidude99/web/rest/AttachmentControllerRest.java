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
import com.latidude99.service.AttachmentService;
import com.latidude99.service.EnquiryService;
import com.latidude99.service.UserService;
import com.latidude99.util.AttachmentStatsRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/*
 * Deals with attachments stored in the database and displayed in detailed enquiry view
 */
@RestController
@RequestMapping("/api")
public class AttachmentControllerRest {
    private static final Logger logger = LoggerFactory.getLogger(AttachmentControllerRest.class);

    @Autowired
    UserService userService;

    @Autowired
    EnquiryService enquiryService;

    @Autowired
    AttachmentService attachmentService;

    @GetMapping(path = "/enquiry/{id}/attachments", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAttachmentsStats(@PathVariable long id){
        List<AttachmentStatsRest> attachmentStatsRests;
        Enquiry enquiry = enquiryService.getById(id);
        if(enquiry != null){
            attachmentStatsRests =
                    attachmentService.getEnquiryAttachmentsStats(enquiry);
        } else {
            ErrorRest errorRest = new ErrorRest(HttpStatus.NOT_FOUND,
                    "Enquiry number " + id + " does not exist",
                    "resource not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorRest);
        }
        return ResponseEntity.ok(attachmentStatsRests);
    }

    @GetMapping(path = "/enquiry/attachment/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getImage(@PathVariable("id") Long id) throws IOException {

        byte[] fileContent = attachmentService.getById(id).getFile();
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.OK);
    }

    @GetMapping(path = "/enquiry/attachment/{id}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadImage(@PathVariable Long id) throws IOException {

        Attachment attachment = attachmentService.getById(id);
        byte[] fileContent = attachment.getFile();
        String filename = attachment.getName();
        final HttpHeaders headers = new HttpHeaders();

        headers.add("content-disposition", "inline;filename=" + filename);
        headers.setContentDispositionFormData(filename, filename);

//      headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.OK);
    }

    @GetMapping(path = "/enquiry/{id}/image") // produces = MediaType.IMAGE_JPEG_VALUE
    public ResponseEntity<?> getPolygonImage(@PathVariable Long id) throws IOException {
        byte[] fileContent = null;
        try {
            Enquiry enquiry = enquiryService.getById(id);
            if(enquiry == null){
                ErrorRest errorRest = new ErrorRest(HttpStatus.NOT_FOUND,
                        "There is no enquiry number " + id,
                        "resource not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorRest);
            }
            fileContent = enquiry.getImage();
            long size = fileContent.length;
        } catch (NullPointerException e) {
            ErrorRest errorRest = new ErrorRest(HttpStatus.NOT_FOUND,
                    "The enquiry number " + id + " has no image saved",
                    "resource not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorRest);
        }
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.OK);
    }


}

	
	
	
	
	
	
	
