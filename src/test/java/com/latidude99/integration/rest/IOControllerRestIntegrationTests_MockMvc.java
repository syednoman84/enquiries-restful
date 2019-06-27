package com.latidude99.integration.rest;

import com.latidude99.model.Enquiry;
import com.latidude99.repository.EnquiryRepository;
import com.latidude99.web.rest.IOControllerRest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static com.latidude99.CustomSecurityMockMvcRequestPostProcessors.demo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/*
 * Mock server
 * @ActiveProfiles("test") - exclude AppConfig class that makes calls to database
 * when starting the app and context wouldn't load with it
 * (secure=false) - to work around Spring Security authentication
 */

@Tag("slow")
//@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "/test.properties")
@AutoConfigureMockMvc(secure=false)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
public class IOControllerRestIntegrationTests_MockMvc {

    @Autowired
    private IOControllerRest ioControllerRest;

    @Autowired
    EnquiryRepository enquiryRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("IOController - isNotNull check")
    public void IOControllerTest_0() {
        assertThat(ioControllerRest).isNotNull();
    }


    @Test
    @DisplayName("IOCOntrollerRest - @/api/enquiry/list/pdf" +
                 " - returns PDF, a list of 100 resent enquiries /success ")
    public void ioControllerTest_1() throws Exception {

        mockMvc.perform(get("/api/enquiry/list/pdf")
                .with(csrf()).with(demo()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF_VALUE))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("IOCOntroller - @/api/enquiry/{id}/pdf" +
                 " - returns printable enquiry page /success")
    public void ioControllerTest_2() throws Exception {

        Enquiry enquiry = enquiryRepository.findById(5);
        assertNotNull(enquiry, "enquiry null before test");

        mockMvc.perform(get("/api/enquiry/5/pdf")
                .with(csrf()).with(demo())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF_VALUE))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("IOCOntroller - @/api/enquiry/{id}/pdf" +
                    " - returns printable enquiry page /failure")
    public void ioControllerTest_3() throws Exception {

        Enquiry enquiry = enquiryRepository.findById(60); // non existent enquiry
        assertNull(enquiry, "enquiry not null before test");

        mockMvc.perform(get("/api/enquiry/60/pdf")
                .with(csrf()).with(demo())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isNotAcceptable());
    }


}






















