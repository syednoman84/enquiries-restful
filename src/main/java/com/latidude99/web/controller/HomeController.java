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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.latidude99.model.Enquiry;
import com.latidude99.model.User;
import com.latidude99.util.EnquiryListWrapper;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        Enquiry enquiry = new Enquiry();
        model.addAttribute("enquiry", enquiry);
        model.addAttribute("user", new User());
        model.addAttribute("uploadFail", null);
        return "enquiryForm";
    }

    @GetMapping("/index")
    public String index() {
        return "redirect: /";
    }

    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }

}












