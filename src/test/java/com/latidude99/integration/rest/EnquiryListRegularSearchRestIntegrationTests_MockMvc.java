package com.latidude99.integration.rest;

import com.latidude99.repository.EnquiryRepository;
import com.latidude99.service.UserService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.latidude99.CustomSecurityMockMvcRequestPostProcessors.demo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/*
 * Mock server
 * @ActiveProfiles("test") - exclude AppConfig class that makes calls to database
 * when starting the app and context wouldn't load with it
 * Uses users defined in CustomSecurityMockMvcRequestPostProcessors class
 * Uses pre-defined entries in DB, loaded from data.sql
 */

@Tag("slow")
//@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "/test.properties")
@AutoConfigureMockMvc(secure=false)
public class EnquiryListRegularSearchRestIntegrationTests_MockMvc {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EnquiryRepository enquiryRepository;

    @Autowired
    private UserService userService;

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
    @DisplayName("EnquiryListRegularSearchRest - @/api/enquiry/search/regular" +
            " -   fetches all enquiries sorted by creation date")
    public void enquiryListControllerTest_1() throws Exception {

        // fetches all 18 enquiries, no specific search criteria set
        this.mockMvc
                .perform(post("/api/enquiry/search/regular")
                        .with(csrf()).with(demo())
                        .param("searchFor", "")
                        .param("searchIn", "all")
                        .param("limit", "0") // 0 = no limit
                        .param("dateRange", "")
                        .param("assignedUser", "any user")
                        .param("closingUser", "any user")
                        .param("status", "all")
                        .param("sortBy", "customer's name") // default=created
                        .param("direction", "ascending")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", Matchers.is(18)))
                .andExpect(jsonPath("$.[0].name", Matchers.is("Brian Wilby")));
    }

    @Test
    @DisplayName("EnquiryListRegularSearchRest - @/api/enquiry/search/regular" +
            " - closingUser = Demo")
    public void enquiryListControllerTest_2() throws Exception {

        // fetches 6 enquiries closed by Demo
        this.mockMvc
                .perform(post("/api/enquiry/search/regular")
                        .with(csrf()).with(demo())
                        .param("searchFor", "")
                        .param("searchIn", "all")
                        .param("limit", "") // default = no limit
                        .param("dateRange", "")
                        .param("assignedUser", "any user")
                        .param("closingUser", "Demo")
                        .param("status", "all")
                        .param("sortBy", "customer's name") // default=created
                        .param("direction", "ascending")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", Matchers.is(6)));
    }

    @Test
    @DisplayName("EnquiryListRegularSearchRest - @/api/enquiry/search/regular " +
            "- assignUser = Lati Test, status = closed")
    public void enquiryListControllerTest_3() throws Exception {

        // fetches 2 enquiries assigned to Lati Test and closed
        this.mockMvc
                .perform(post("/api/enquiry/search/regular")
                        .with(csrf()).with(demo())
                        .param("searchFor", "")
                        .param("searchIn", "all")
                        .param("limit", "") // default = no limit
                        .param("dateRange", "")
                        .param("assignedUser", "Lati Test")
                        .param("closingUser", "any user")
                        .param("status", "closed")
                        .param("sortBy", "customer's name") // default=created
                        .param("direction", "ascending")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", Matchers.is(2)));
    }

    @Test
    @DisplayName("EnquiryListRegularSearchRest - @/api/enquiry/search/regular " +
            "- assignUser = Lati Test, status = in progress, dateRange = 01/07/2018 - 31/07/2018")
    public void enquiryListControllerTest_4() throws Exception {

        // 3 enquiries assign to Lati Test, in progress and within 01/07/2018 - 31/07/2018
        this.mockMvc
                .perform(post("/api/enquiry/search/regular")
                        .with(csrf()).with(demo())
                        .param("searchFor", "")
                        .param("searchIn", "all")
                        .param("limit", "") // default = no limit
                        .param("dateRange", "01/07/2018 - 31/07/2018")
                        .param("assignedUser", "Lati Test")
                        .param("closingUser", "any user")
                        .param("status", "in progress")
                        .param("sortBy", "customer's name") // default=created
                        .param("direction", "ascending")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", Matchers.is(3)));
    }

    @Test
    @DisplayName("EnquiryListRegularSearch - @/enquiry/search/regular - assignUser = Lati Test, status = in progress," +
            " dateRange = 01/07/2018 - 31/07/2018, customer name = Sarah Shopland")
    public void enquiryListControllerTest_5() throws Exception {

        this.mockMvc
                .perform(post("/api/enquiry/search/regular")
                        .with(csrf()).with(demo())
                        .param("searchFor", "Sarah Shopland")
                        .param("searchIn", "in customer names")
                        .param("limit", "") // default = no limit
                        .param("dateRange", "01/07/2018 - 31/07/2018")
                        .param("assignedUser", "Lati Test")
                        .param("closingUser", "any user")
                        .param("status", "in progress")
                        .param("sortBy", "customer's name") // default=created
                        .param("direction", "ascending")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", Matchers.is(1)))
                .andExpect(jsonPath("$.[0].name", Matchers.is("Sarah Shopland")));
    }

}














