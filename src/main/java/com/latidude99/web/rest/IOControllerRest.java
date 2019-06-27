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
import com.latidude99.service.EnquiryService;
import com.latidude99.service.UserService;
import com.latidude99.util.GenerateEnquiryListPdfFromCode;
import com.latidude99.util.PdfCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/*
 * Deals with creating and downloading a PDF version of an enquiry
 * and a list of 100 recent enquiries
 */
@RestController
@RequestMapping("/api")
public class IOControllerRest {
    private static final Logger logger = LoggerFactory.getLogger(IOControllerRest.class);

    @Autowired
    UserService userService;

    @Autowired
    EnquiryService enquiryService;

    @Autowired
    PdfCreator pdfCreator;


    @GetMapping(path = "/enquiry/list/pdf",produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> enquiryReport() throws IOException{
        List<Enquiry> enquiries = enquiryService.getRecent100Sorted();
        HttpHeaders headers;
        ByteArrayInputStream bis = null;
        try{
            bis = GenerateEnquiryListPdfFromCode.enquiryListReport(enquiries);
            headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=citiesreport.pdf");
        } catch (Exception e){
            ErrorRest errorRest = new ErrorRest(HttpStatus.EXPECTATION_FAILED,
                    "Error creating PDF, Exception ocurred",
                    e.getLocalizedMessage());
            return ResponseEntity
                    .status(HttpStatus.EXPECTATION_FAILED)
                    .body(errorRest);
        } finally{
            bis.close();
        }
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));

    }

    @GetMapping(path = "/enquiry/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<?> enquiryPdf(@PathVariable long id) throws IOException {

        Enquiry enquiryToPDF = enquiryService.getById(id);
        enquiryService.sortProgressUsers(enquiryToPDF);

        ByteArrayInputStream bis = null;
        try {
            Map<String, Enquiry> data = new HashMap<>();
            data.put("enquiry", enquiryToPDF);
            bis = pdfCreator.createPdf("enquiryPagePdf", data);
        } catch (Exception e) {
            ErrorRest errorRest = new ErrorRest(HttpStatus.EXPECTATION_FAILED,
                    "Error creating PDF, Exception ocurred",
                    e.getLocalizedMessage());
            return ResponseEntity
                    .status(HttpStatus.EXPECTATION_FAILED)
                    .body(errorRest);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=enquiry.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }


}
























