package com.latidude99.integration.rest;

import com.latidude99.web.rest.AuthenticationControllerRest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 *  - Mock server
 *  - @ActiveProfiles("test") - exclude AppConfig class that makes calls to
 *    database when starting the app and context wouldn't load with it
 *  - (secure=false) - to work around Spring Security authentication
 */

@Tag("slow")
//@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "/test.properties")
@AutoConfigureMockMvc(secure=false)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
public class AuthenticationControllerRestIntegrationTests_MockMvc {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AuthenticationControllerRest authenticationControllerRest;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("AuthenticationControllerRest - isNotNull check")
    public void authenticationControllerRestTest_0() {
        assertThat(authenticationControllerRest).isNotNull();
    }


    /*
     * AuthenticationControllerRest returns Principal object
     * with the logged in user details in its properties
     */
    @Test
    @DisplayName("AuthenticationControllerRest - @/login -checks logged in user properties")
    @WithMockUser(username = "demo@demo.com", password = "111111", roles = "USER")
    public void userControllerTest_1() throws Exception {

        mockMvc.perform(post("/login")) //.with(csrf()).with(demo()))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.authorities.[0].authority", Matchers.is("ROLE_USER")))
                .andExpect(jsonPath("$.authenticated", Matchers.is(true)))
                 // MockMvc includes non-encoded password in response
                .andExpect(jsonPath("$.principal.password", Matchers.is("111111")))
                .andExpect(jsonPath("$.principal.username", Matchers.is("demo@demo.com")))
                .andExpect(jsonPath(
                        "$.principal.authorities.[0].authority", Matchers.is("ROLE_USER")))
                .andExpect(jsonPath("$.principal.enabled", Matchers.is(true)))
                .andExpect(jsonPath("$.name", Matchers.is("demo@demo.com")))
                ;
    }

}















