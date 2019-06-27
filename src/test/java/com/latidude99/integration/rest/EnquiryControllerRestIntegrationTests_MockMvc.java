package com.latidude99.integration.rest;

import com.latidude99.web.rest.EnquiryControllerRest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.latidude99.CustomSecurityMockMvcRequestPostProcessors.demo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/*
 *  - Mock server
 *  - @ActiveProfiles("test") - exclude AppConfig class that makes calls to
 *    database when starting the app and context wouldn't load with it
 *  - (secure=false) - to work around Spring Security authentication
 *  - tests Enquiry image property at http://localhost:8080/image/4 (test3)
 *  - uses pre-defined entries in DB, loaded from data.sql
 */

@Tag("slow")
//@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "/test.properties")
@AutoConfigureMockMvc(secure=false)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class EnquiryControllerRestIntegrationTests_MockMvc {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EnquiryControllerRest enquiryControllerRest;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    public static void initAll(){
    }

    @AfterAll
    public static void tearDownAll(){
    }

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("EnquiryControllerRest - isNotNull check")
    public void enquiryControllerTest_0() {
        assertThat(enquiryControllerRest).isNotNull();
    }


    @Test
    @DisplayName("EnquiryControllerRest - @/api/enquiry/{id} - fetches enquiry by id")
    @WithMockUser(username = "demo@demo.com", password = "111111", roles = "USER")
    public void enquiryControllerTest_1() throws Exception {

        // existing
        mockMvc.perform(get("/api/enquiry/12")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(12)));

        // non existent
        mockMvc.perform(get("/api/enquiry/34")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }



    @Test
    @DisplayName("EnquiryControllerRest - @/api/enquiry/{id}/comment" +
            " - submits a comment to the id enquiry")
    public void enquiryControllerTest_2() throws Exception {

        this.mockMvc
                .perform(post("/api/enquiry/4/comment")
                        .with(csrf()).with(demo())
                        .param("comment", "controller test @/enquiry/comment")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email",
                        Matchers.is("janros@hotmail.co.uk"))) // customer's email
                .andExpect(jsonPath("$.comments.[4].content",
                        Matchers.is("controller test @/enquiry/comment"))); // the new comment
    }

    @Test
    @DisplayName("EnquiryControllerRest - @/api/enquiry/{id}/comment" +
            " - submits a comment to the non existent id enquiry")
    public void enquiryControllerTest_3() throws Exception {

        // non existent enquiry
        this.mockMvc
                .perform(post("/api/enquiry/26/comment")
                        .with(csrf()).with(demo())
                        .param("comment", "controller test @/enquiry/comment")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message",
                        Matchers.is("Enquiry number " + "26" + " does not exist")))
                .andExpect(jsonPath("$.errors.[0]",
                        Matchers.is("resource not found")));
    }

    @Test
    @DisplayName("EnquiryControllerRest - @/api/enquiry/{id}/email" +
            " - sends an email with the id enquiry " +
            "(if no email specified or email='' sends it to the logged in user")
    public void enquiryControllerTest_5() throws Exception {

        this.mockMvc
                .perform(post("/api/enquiry/4/email")
                        .with(csrf()).with(demo())
                        .param("email", "")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(4)));
    }


    @Test
    @DisplayName("EnquiryControllerRest - @/api/enquiry/{id}/assign" +
            " - assigns enquiry to the current user")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void enquiryControllerTest_10() throws Exception {

        this.mockMvc
                .perform(get("/api/enquiry/5/assign")
                        .with(csrf()).with(demo())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waiting", Matchers.is(5))) // -1
                .andExpect(jsonPath("$.opened", Matchers.is(7))) // +1
                .andExpect(jsonPath("$.closed", Matchers.is(6)))
                .andExpect(jsonPath("$.assignedToUser", Matchers.is(5))) // +1
                .andExpect(jsonPath("$.enquiry.status",
                        Matchers.is("in progress")))
                .andExpect(jsonPath("$.enquiry.sortedProgressUsersWithDate.[0]",
                        Matchers.containsString("Demo")));
    }

    @Test
    @DisplayName("EnquiryControllerRest - @/api/enquiry/{id}/deassign " +
            "- deassigns enquiry from the current user")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void enquiryControllerTest_11() throws Exception {

        this.mockMvc
                .perform(get("/api/enquiry/9/deassign")
                        .with(csrf()).with(demo())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waiting", Matchers.is(6))) // no change
                .andExpect(jsonPath("$.opened", Matchers.is(6))) // no change
                .andExpect(jsonPath("$.closed", Matchers.is(6)))
                .andExpect(jsonPath("$.assignedToUser", Matchers.is(3))) // -1
                .andExpect(jsonPath("$.enquiry.status",
                        Matchers.is("in progress")))
                .andExpect(jsonPath("$.enquiry.sortedProgressUsersWithDate.length()",
                        Matchers.is(2)));
    }

    /*
     * @/enquiry/close -> "assignedToUser" value decreases by 1
     */
    @Test
    @DisplayName("EnquiryControllerRest - @/api/enquiry/{id}/close " +
            "- closes the enquiry")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void enquiryControllerTest_12() throws Exception {

        this.mockMvc
                .perform(get("/api/enquiry/9/close")
                        .with(csrf()).with(demo())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waiting", Matchers.is(6))) // no change
                .andExpect(jsonPath("$.opened", Matchers.is(5))) // -1
                .andExpect(jsonPath("$.closed", Matchers.is(7))) // +1
                .andExpect(jsonPath("$.assignedToUser", Matchers.is(3))) // -1
                .andExpect(jsonPath("$.closedByUser", Matchers.is(7)))
                .andExpect(jsonPath("$.enquiry.status",
                        Matchers.is("closed")))
                .andExpect(jsonPath("$.enquiry.sortedProgressUsersWithDate.length()",
                        Matchers.is(3)))
                .andExpect(jsonPath("$.enquiry.closingUser.name",
                        Matchers.is("Demo")));
    }

    /*
     * @/enquiry/open -> "assignedToUser" value doesn't change because
     * open =/= assign and "assignedToUser" really means "assign" to the user
     * (makes sense when one user opens an enquiry not assign to them and doesn't
     * want to be assign to it but only marked it as not finished)
     * Will consider refactoring the names here a bit.
     */
    @Test
    @DisplayName("EnquiryControllerRest - @/api/enquiry/{id}/open " +
            "- opens the enquiry without assigning to user")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void enquiryControllerTest_13() throws Exception {

        this.mockMvc
                .perform(get("/api/enquiry/4/open").with(csrf()).with(demo())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waiting", Matchers.is(5))) // -1
                .andExpect(jsonPath("$.opened", Matchers.is(7))) // +1
                .andExpect(jsonPath("$.closed", Matchers.is(6))) // no change
                .andExpect(jsonPath("$.assignedToUser", Matchers.is(4))) // no change!
                .andExpect(jsonPath("$.closedByUser", Matchers.is(6))) // no change
                .andExpect(jsonPath("$.enquiry.status",
                        Matchers.is("in progress")))
                .andExpect(jsonPath("$.enquiry.sortedProgressUsersWithDate.length()",
                        Matchers.is(0))); //no change!
    }




}










