package com.latidude99.integration;

import com.latidude99.model.Comment;
import com.latidude99.model.Enquiry;
import com.latidude99.model.User;
import com.latidude99.util.FormBean;
import com.latidude99.web.controller.EnquiryController;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.latidude99.CustomSecurityMockMvcRequestPostProcessors.demo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/*
 *  - Mock server
 *  - @ActiveProfiles("test") - exclude AppConfig class that makes calls to database
 *  - when starting the app and context wouldn't load with it
 *  - (secure=false) - to work around Spring Security authentication
 *  - tests Enquiry image property at http://localhost:8080/image/4
 */

@Tag("slow")
@Tag("serverMock")
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "/test.properties")
@AutoConfigureMockMvc(secure=false)
public class EnquiryControllerIntegrationTest {

    /*
     * Passing Model object as attribute in a test POST request is not necessary
     * (despite forms having an object bound - Thymeleaf)
     */
    static Enquiry enquiry = new Enquiry();
    static FormBean formBean = new FormBean();

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EnquiryController enquiryController;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public static void initAll(){
//        enquiry.setId(5L);
//        enquiry.setProgressUser(new HashMap<Date, User>() {});
//        formBean.setCommentContent("controller @/enquiry/comment"); // not working
    }

    @AfterAll
    public static void tearDownAll(){
//        enquiry = null;
    }

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("EnquiryController - isNotNull check")
    public void enquiryControllerTest_0() {
        assertThat(enquiryController).isNotNull();
    }


    @Test
    @DisplayName("EnquiryController - response check")
    @WithMockUser(username = "demo@demo.com", password = "111111", roles = "USER")
    public void enquiryControllerTest_1() throws Exception {

        mockMvc.perform(post("/enquiry/page").with(csrf())//.with(demo())
//                .flashAttr("enquiry", enquiry)
                .param("id", "5")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryPage"))
        .andReturn();
    }


    @Test
    @DisplayName("EnquiryController - @/enquiry/page - status and model attributes exist")
    public void enquiryControllerTest_2() throws Exception {

        this.mockMvc
                .perform(post("/enquiry/page").with(csrf()).with(demo())
//                        .flashAttr("enquiry", enquiry) // not necessary
                        .param("id", "5")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryPage"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attributeExists("currentUser"))
                .andExpect(model().attributeExists("enquiry"))
                .andExpect(model().attributeExists("waiting"))
                .andExpect(model().attributeExists("opened"))
                .andExpect(model().attributeExists("closed"))
                .andExpect(model().attributeExists("openedByUser"))
                .andExpect(model().attributeExists("assignedToUserAndClosed"))
                .andExpect(model().attributeExists("closedByUser"))
                .andExpect(model().attributeExists("imageDbUrl"))
                .andExpect(model().attribute("email", nullValue()))
                .andExpect(model().attribute("emailFail", nullValue()))
                .andExpect(content().string(containsString("kris.triggle@googlemail.com"))) // enquiry 5
                .andReturn();

    }

    @Test
    @DisplayName("EnquiryController - @/enquiry/page - model attributes values")
    public void enquiryControllerTest_3() throws Exception {

        this.mockMvc
                .perform(post("/enquiry/page").with(csrf()).with(demo())
                        .param("id", "4")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryPage"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attributeExists("currentUser"))
                .andExpect(model().attributeExists("enquiry"))
                .andExpect(model().attribute("waiting", "6"))
                .andExpect(model().attribute("opened", "6"))
                .andExpect(model().attribute("closed", "6"))
                .andExpect(model().attribute("openedByUser", "4"))
                .andExpect(model().attribute("assignedToUserAndClosed", "6"))
                .andExpect(model().attribute("closedByUser", "6"))
                .andExpect(model().attribute("imageDbUrl", "http://localhost:8080/image/4"))
                .andExpect(model().attribute("email", nullValue()))
                .andExpect(model().attribute("emailFail", nullValue()))
                .andExpect(content().string(containsString("janros@hotmail.co.uk"))) // enquiry 5
                .andReturn();

    }

    @Test
    @DisplayName("EnquiryController - @/enquiry/comment - attributes exist and form submition")
    public void enquiryControllerTest_4() throws Exception {

        this.mockMvc
                .perform(post("/enquiry/comment").with(csrf()).with(demo())
//                        .flashAttr("enquiry", formBean) // not necessary
                        .param("enquiryId", "4")
                        .param("userId", "4")
                        .param("commentContent", "controller test @/enquiry/comment")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryPage"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attributeExists("currentUser"))
                .andExpect(model().attributeExists("enquiry"))
                .andExpect(model().attributeExists("waiting"))
                .andExpect(model().attributeExists("opened"))
                .andExpect(model().attributeExists("closed"))
                .andExpect(model().attributeExists("openedByUser"))
                .andExpect(model().attributeExists("assignedToUserAndClosed"))
                .andExpect(model().attributeExists("closedByUser"))
                .andExpect(model().attributeExists("imageDbUrl"))
                .andExpect(model().attribute("email", nullValue()))
                .andExpect(model().attribute("emailFail", nullValue()))
                .andExpect(content().string(containsString("janros@hotmail.co.uk"))) // customer's email
                .andExpect(content().string(containsString("controller test @/enquiry/comment"))) // new comment
                .andReturn();

    }

    @Test
    @DisplayName("EnquiryController - @/enquiry/email - attributes exist and email sending")
    public void enquiryControllerTest_5() throws Exception {

        this.mockMvc
                .perform(post("/enquiry/email").with(csrf()).with(demo())
                        .param("id", "4")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryPage"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attributeExists("currentUser"))
                .andExpect(model().attributeExists("enquiry"))
                .andExpect(model().attributeExists("waiting"))
                .andExpect(model().attributeExists("opened"))
                .andExpect(model().attributeExists("closed"))
                .andExpect(model().attributeExists("openedByUser"))
                .andExpect(model().attributeExists("assignedToUserAndClosed"))
                .andExpect(model().attributeExists("closedByUser"))
                .andExpect(model().attributeExists("imageDbUrl"))
                .andExpect(model().attribute("email", "The enquiry has been successfully emailed to: "))
                .andExpect(model().attribute("emailFail", nullValue()))
                .andExpect(content().string(containsString("janros@hotmail.co.uk"))) // customer's email
                .andExpect(content().string(
                        containsString("The enquiry has been successfully emailed to: "))) // new comment
                .andReturn();

    }

}



























