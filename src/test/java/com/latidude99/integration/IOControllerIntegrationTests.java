package com.latidude99.integration;

import com.latidude99.model.Enquiry;
import com.latidude99.model.UserRole;
import com.latidude99.repository.EnquiryRepository;
import com.latidude99.web.controller.EnquiryController;
import com.latidude99.web.controller.HomeController;
import com.latidude99.web.controller.IOController;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static com.latidude99.CustomSecurityMockMvcRequestPostProcessors.demo;
import static com.latidude99.CustomSecurityMockMvcRequestPostProcessors.latiTest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/*
 * Mock server
 * @ActiveProfiles("test") - exclude AppConfig class that makes calls to database
 * when starting the app and context wouldn't load with it
 * (secure=false) - to work around Spring Security authentication
 */

@Tag("slow")
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "/test.properties")
@AutoConfigureMockMvc(secure=false)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
public class IOControllerIntegrationTests {

    @Autowired
    private IOController ioController;

    @Autowired
    EnquiryRepository enquiryRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("IOController - isNotNull check")
    public void IOControllerTest_0() {
        assertThat(ioController).isNotNull();
    }

    @Test
    @DisplayName("IOCOntroller - @/enquiry/printable - returns printable enquiry page")
    public void homeControllerTest_1() throws Exception {

        Enquiry enquiry = enquiryRepository.findById(5);

        mockMvc.perform(post("/enquiry/printable").with(csrf()).with(demo())
                .flashAttr("user", enquiry)
                .param("id", "" + enquiry.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryPagePrintable"))
                .andExpect(content().string(Matchers.containsString("kris.triggle@googlemail.com")))
                .andExpect(model().attribute("enquiry",
                        Matchers.hasProperty("email",
                                Matchers.containsString("kris.triggle@googlemail.com"))))
                .andExpect(model().attribute("enquiry",
                        Matchers.hasProperty("polygon", Matchers.notNullValue())));
    }

    @Test
    @DisplayName("IOCOntroller - @/enquiry/list100/pdf - returns PDF, a list of 100 resent enquiries ")
    public void homeControllerTest_2() throws Exception {


        mockMvc.perform(get("/enquiry/list100/pdf").with(csrf()).with(demo()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF_VALUE))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("IOCOntroller - @/enquiry/pdf - returns PDF of the opened enquiry")
    public void homeControllerTest_3() throws Exception {

        Enquiry enquiry = enquiryRepository.findById(5);

        MvcResult result = mockMvc.perform(post("/enquiry/pdf").with(csrf()).with(demo())
                .flashAttr("user", enquiry)
                .param("id", "" + enquiry.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF_VALUE))
                .andReturn();

    }

}






















