package com.latidude99.integration.rest;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.latidude99.web.rest.EnquiryControllerRest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.latidude99.CustomSecurityMockMvcRequestPostProcessors.demo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
public class EnquiryListControllerRestIntegrationTests_MockMvc {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EnquiryControllerRest enquiryListControllerRest;

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
    @DisplayName("EnquiryListControllerRest_test0 - isNotNull check")
    @WithMockUser(username = "Demo", password = "111111", roles = "USER")
    public void enquiryListControllerTest_0() {
        assertThat(enquiryListControllerRest).isNotNull();
    }

    @Test
    @DisplayName("EnquiryListControllerRest_test1 - @/api/enquiry/list " +
            "- checks returned list of enquiries")
    public void enquiryListControllerTest_1() throws Exception {

        mockMvc.perform(get("/api/enquiry/list")
                .with(csrf()).with(demo()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waiting", Matchers.is(6)))
                .andExpect(jsonPath("$.opened", Matchers.is(6)))
                .andExpect(jsonPath("$.closed", Matchers.is(6)))
                .andExpect(jsonPath("$.enquiryList.length()",
                        Matchers.is(18)))
                .andExpect(jsonPath("$.enquiryList.[0].name",
                        Matchers.is("Roderick Smith")))
                .andExpect(jsonPath("$.enquiryList.[0].email",
                        Matchers.is("rix38@btinternet.com")))
                .andExpect(jsonPath("$.enquiryList.[0].phone",
                        Matchers.is("07345432333")))
                .andExpect(jsonPath("$.enquiryList.[0].type",
                        Matchers.is("Customised Mapping")));
    }

    @Test
    @DisplayName("EnquiryListControllerRest_test2 - @/api/enquiry/list/stats " +
            "- checks stats of enquiries")
    public void enquiryListControllerTest_2() throws Exception {

        mockMvc.perform(get("/api/enquiry/list/stats")
                .with(csrf()).with(demo()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waiting", Matchers.is(6)))
                .andExpect(jsonPath("$.opened", Matchers.is(6)))
                .andExpect(jsonPath("$.closed", Matchers.is(6)))
                .andExpect(jsonPath("$.assignedToUser", Matchers.is(4)))
                .andExpect(jsonPath("$.closedByUser", Matchers.is(6)));
    }

    @Test
    @DisplayName("EnquiryListControllerRest_test3 - @/api/enquiry/list/last/{number} " +
            "- checks returned list of enquiries")
    public void enquiryListControllerTest_3() throws Exception {

        int customNum = 12;

        mockMvc.perform(get("/api/enquiry/list/last/" + customNum)
                .with(csrf()).with(demo()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", Matchers.is(customNum)));
    }

    @Test
    @DisplayName("EnquiryListControllerRest_test4 - @/enquiry/list/user/{fetchBy} " +
            "- checks returned list of enquiries")
    public void enquiryListControllerTest_4() throws Exception {
        String fetchBy;
        int enquiriesFetched;
        String json;
        DocumentContext jsonContext;
        MvcResult mvcResult;

        fetchBy = "assigned";
        enquiriesFetched = 4;
        mvcResult =
                mockMvc.perform(get("/api/enquiry/list/user/" + fetchBy)
                .with(csrf()).with(demo()))
                .andDo(print())
                .andExpect(status().isOk())
//              .andExpect(jsonPath("$.length()", Matchers.is(enquiriesFetched))) // can't include error message
                .andReturn();

        json = mvcResult.getResponse().getContentAsString();
        jsonContext = JsonPath.parse(json);
        assertEquals((int)jsonContext.read("$.length()"), enquiriesFetched,
                "incorrect number of 'assigned' enquiries");


        fetchBy = "closedbyother";
        enquiriesFetched = 6;
        mvcResult =
                mockMvc.perform(get("/api/enquiry/list/user/" + fetchBy)
                .with(csrf()).with(demo()))
                .andDo(print())
                .andExpect(status().isOk())
//              .andExpect(jsonPath("$.length()user/", Matchers.is(enquiriesFetched)))
                .andReturn();

        json = mvcResult.getResponse().getContentAsString();
        jsonContext = JsonPath.parse(json);
        assertEquals((int)jsonContext.read("$.length()"), enquiriesFetched,
                "incorrect number of 'closedbyother' enquiries");


        fetchBy = "closedbyuser";
        enquiriesFetched = 6;
        mvcResult =
                mockMvc.perform(get("/api/enquiry/list/user/" + fetchBy)
                .with(csrf()).with(demo()))
                .andDo(print())
                .andExpect(status().isOk())
//              .andExpect(jsonPath("$.length()", Matchers.is(enquiriesFetched)))
                .andReturn();
        json = mvcResult.getResponse().getContentAsString();
        jsonContext = JsonPath.parse(json);
        assertEquals((int)jsonContext.read("$.length()"), enquiriesFetched,
                "incorrect number of 'closedbyuser' enquiries");


        fetchBy = "incorrect value here";
        enquiriesFetched = 0;
        mvcResult =
                mockMvc.perform(get("/api/enquiry/list/user/" + fetchBy)
                .with(csrf()).with(demo()))
                .andDo(print())
                .andExpect(status().isOk())
//              .andExpect(jsonPath("$.length()", Matchers.is(enquiriesFetched)))
                .andReturn();

        json = mvcResult.getResponse().getContentAsString();
        jsonContext = JsonPath.parse(json);
        assertEquals((int)jsonContext.read("$.length()"), enquiriesFetched,
                "incorrect number of 'incorrect value' enquiries");
    }

    @Test
    @DisplayName("EnquiryListControllerRest_test5 - @/enquiry/search/ids" +
            " -   returns specified enquiries by their ids")
    public void enquiryListControllerTest_5() throws Exception {
        String json;
        DocumentContext jsonContext;
        MvcResult mvcResult;
        String ids;
        int enquiriesFetched;

        ids = "5, 7"; // 2 existng enquiries
        enquiriesFetched = 2;
        mvcResult =
                mockMvc
                        .perform(post("/api/enquiry/search/ids")
                                .with(csrf()).with(demo())
                                .param("ids", ids))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn();

        json = mvcResult.getResponse().getContentAsString();
        jsonContext = JsonPath.parse(json);
        assertEquals((int)jsonContext.read("$.length()"), enquiriesFetched,
                "incorrect number of enquiries fetched /ids='5, 7'");


        ids = "12-15"; // 4 existng enquiries
        enquiriesFetched = 4;
        mvcResult =
                mockMvc
                        .perform(post("/api/enquiry/search/ids")
                                .with(csrf()).with(demo())
                                .param("ids", ids))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn();

        json = mvcResult.getResponse().getContentAsString();
        jsonContext = JsonPath.parse(json);
        assertEquals((int)jsonContext.read("$.length()"), enquiriesFetched,
                "incorrect number of enquiries fetched /ids='5, 7'");


        ids = "24-26"; // 0 existng enquiries
        enquiriesFetched = 0;
        mvcResult =
                mockMvc
                        .perform(post("/api/enquiry/search/ids")
                                .with(csrf()).with(demo())
                                .param("ids", ids))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn();

        json = mvcResult.getResponse().getContentAsString();
        jsonContext = JsonPath.parse(json);
        assertEquals((int)jsonContext.read("$.length()"), enquiriesFetched,
                "incorrect number of enquiries fetched /ids='5, 7'");


        ids = "5,2,4,24-26, 14-16, 56,34, 45-48"; // 6 existng enquiries
        enquiriesFetched = 6;
        mvcResult =
                mockMvc
                        .perform(post("/api/enquiry/search/ids")
                                .with(csrf()).with(demo())
                                .param("ids", ids))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn();

        json = mvcResult.getResponse().getContentAsString();
        jsonContext = JsonPath.parse(json);
        assertEquals((int)jsonContext.read("$.length()"), enquiriesFetched,
                "incorrect number of enquiries fetched /ids='5, 7'");

    }



}












