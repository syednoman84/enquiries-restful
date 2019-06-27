package com.latidude99.web.rest;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationControllerRest {

    /*
     * Checks logged in user details
     */
    @PostMapping("/login")
    public Principal login(Principal user) {
        return user;
    }
}
