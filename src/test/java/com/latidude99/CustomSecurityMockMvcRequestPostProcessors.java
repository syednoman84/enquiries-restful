package com.latidude99;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

public class CustomSecurityMockMvcRequestPostProcessors {

    public static RequestPostProcessor demo() {
        return user("demo@demo.com").password("111111").roles("USER");
    }

    public static RequestPostProcessor latiDude() {
        return user("latidude99@gmail.com").password("0011100").roles("APPADMIN");
    }

    public static RequestPostProcessor latiTest() {
        return user("latidude99test@gmail.com").password("1100011").roles("ADMIN");
    }


}
