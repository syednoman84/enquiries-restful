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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.latidude99.model.Attachment;
import com.latidude99.service.AttachmentService;
import com.latidude99.service.EnquiryService;
import com.latidude99.service.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
 * Deals with attachments stored in the database and displayed in detailed enquiry view
 */
@Controller
public class AttachmentController {
    private static final Logger logger = LoggerFactory.getLogger(AttachmentController.class);

    @Autowired
    UserService userService;

    @Autowired
    EnquiryService enquiryService;

    @Autowired
    AttachmentService attachmentService;

    @RequestMapping(value = "/attachment/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getImage(@PathVariable("id") Long id) throws IOException {

        byte[] fileContent = attachmentService.getById(id).getFile();
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/attachment/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadImage(@RequestParam Long id) throws IOException {

        Attachment attachment = attachmentService.getById(id);
        byte[] fileContent = attachment.getFile();
        String filename = attachment.getName();
        final HttpHeaders headers = new HttpHeaders();

        headers.add("content-disposition", "inline;filename=" + filename);
        headers.setContentDispositionFormData(filename, filename);

//      headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/image/{id}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getPolygonImage(@PathVariable("id") Long id) throws IOException {
        byte[] fileContent = null;
        try {
            fileContent = enquiryService.getById(id).getImage();
            System.out.println("Enquiry no. " + id + ", image size: " + enquiryService.getById(id).getImage().length);
        } catch (NullPointerException e) {
            System.out.println("No saved coverage image");
        }
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<byte[]>(fileContent, headers, HttpStatus.OK);
    }


}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	