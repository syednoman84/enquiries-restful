package com.latidude99.integration;

import com.latidude99.model.Enquiry;
import com.latidude99.model.User;
import com.latidude99.repository.EnquiryRepository;
import com.latidude99.repository.UserRepository;
import com.latidude99.service.UserService;
import com.latidude99.util.FormBean;
import com.latidude99.util.SearchWrapper;
import com.latidude99.web.controller.EnquiryController;
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
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/*
 * Mock server
 * @ActiveProfiles("test") - exclude AppConfig class that makes calls to database
 * when starting the app and context wouldn't load with it
 * Uses users defined in CustomSecurityMockMvcRequestPostProcessors class
 * Uses pre-defined entries in DB, loaded from data.sql
 */

@Tag("slow")
@Tag("serverMock")
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "/test.properties")
@AutoConfigureMockMvc(secure=false)
public class EnquiryListRegularSearchIntegrationTests {

    SearchWrapper searchWrapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EnquiryController enquiryListController;

    @Autowired
    private EnquiryRepository enquiryRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void init() {

        // same values set as for the @ModelAttribute in the EnquiryListController
        searchWrapper = new SearchWrapper();
        searchWrapper.setSearchFor("");
        searchWrapper.setSearchIn("all");
        searchWrapper.setLimit(0);
        searchWrapper.setUserList(userService.getUserListAsStringList());
        searchWrapper.setAssignedUser("any user");
        searchWrapper.setClosingUser("any user");
        searchWrapper.setStatus("all");
        searchWrapper.setSortBy("all");
        searchWrapper.setDirection("ascending");
        searchWrapper.setSelector("keywordWildcard"); // not used in Regular search (used in FullTextSearch)

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }



    @Test
    @DisplayName("EnquiryListRegularSearch - @/enquiry/search/regular -   search params bean deafault values ")
    public void enquiryListControllerTest_1() throws Exception {

        this.mockMvc
                .perform(post("/enquiry/search/regular").with(csrf()).with(demo())
                        .flashAttr("searchWrapper", searchWrapper)
                        .param("searchFor", searchWrapper.getSearchFor())
                        .param("searchIn", searchWrapper.getSearchIn())
                        .param("limit", "" + searchWrapper.getLimit())
                        .param("dateRange", "")  // value from the post form
                        .param("assignedUser", searchWrapper.getAssignedUser())
                        .param("closingUser", searchWrapper.getClosingUser())
                        .param("status", searchWrapper.getStatus())
                        .param("sortBy", "creation date") // value in post form
                        .param("direction", searchWrapper.getDirection())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryList"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("searchFor", Matchers.containsString(""))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("searchIn", Matchers.containsString("all"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("limit", Matchers.equalTo(0))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("dateRange", Matchers.containsString(""))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("assignedUser", Matchers.containsString("any user"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("closingUser", Matchers.containsString("any user"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("status", Matchers.containsString("all"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("sortBy", Matchers.containsString("creation date"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("direction", Matchers.containsString("ascending"))))
                .andExpect(model().attribute("enquiryListWrapper",
                        Matchers.hasProperty( "enquiryList", Matchers
                                .hasSize(18)))); // 18 enquiries on the list, no specific search criteria set
    }

    @Test
    @DisplayName("EnquiryListRegularSearch - @/enquiry/search/regular - closingUser = Demo")
    public void enquiryListControllerTest_2() throws Exception {

        searchWrapper.setClosingUser("Demo");

        this.mockMvc
                .perform(post("/enquiry/search/regular").with(csrf()).with(demo())
                        .flashAttr("searchWrapper", searchWrapper)
                        .param("searchFor", searchWrapper.getSearchFor())
                        .param("searchIn", searchWrapper.getSearchIn())
                        .param("limit", "" + searchWrapper.getLimit())
                        .param("dateRange", "")  // value from the post form
                        .param("assignedUser", searchWrapper.getAssignedUser())
                        .param("closingUser", searchWrapper.getClosingUser())
                        .param("status", searchWrapper.getStatus())
                        .param("sortBy", "creation date") // value in post form
                        .param("direction", searchWrapper.getDirection())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryList"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("searchFor", Matchers.containsString(""))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("searchIn", Matchers.containsString("all"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("limit", Matchers.equalTo(0))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("dateRange", Matchers.containsString(""))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("assignedUser", Matchers.containsString("any user"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("closingUser", Matchers.containsString("Demo"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("status", Matchers.containsString("all"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("sortBy", Matchers.containsString("creation date"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("direction", Matchers.containsString("ascending"))))
                .andExpect(model().attribute("enquiryListWrapper",
                        Matchers.hasProperty( "enquiryList", Matchers
                                .hasSize(6)))); // 6 enquiries closed by Demo
    }

    @Test
    @DisplayName("EnquiryListRegularSearch - @/enquiry/search/regular - assignUser = Lati Test, status = closed")
    public void enquiryListControllerTest_3() throws Exception {

        searchWrapper.setAssignedUser("Lati Test");
        searchWrapper.setStatus("closed");

        this.mockMvc
                .perform(post("/enquiry/search/regular").with(csrf()).with(demo())
                        .flashAttr("searchWrapper", searchWrapper)
                        .param("searchFor", searchWrapper.getSearchFor())
                        .param("searchIn", searchWrapper.getSearchIn())
                        .param("limit", "" + searchWrapper.getLimit())
                        .param("dateRange", "")  // value from the post form
                        .param("assignedUser", searchWrapper.getAssignedUser())
                        .param("closingUser", searchWrapper.getClosingUser())
                        .param("status", searchWrapper.getStatus())
                        .param("sortBy", "creation date") // value in post form
                        .param("direction", searchWrapper.getDirection())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryList"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("searchFor", Matchers.containsString(""))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("searchIn", Matchers.containsString("all"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("limit", Matchers.equalTo(0))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("dateRange", Matchers.containsString(""))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("assignedUser", Matchers.containsString("Lati Test"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("closingUser", Matchers.containsString("any user"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("status", Matchers.containsString("closed"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("sortBy", Matchers.containsString("creation date"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("direction", Matchers.containsString("ascending"))))
                .andExpect(model().attribute("enquiryListWrapper",
                        Matchers.hasProperty( "enquiryList", Matchers
                                .hasSize(2)))); // 2 enquiries assign to Lati Test and closed
    }

    @Test
    @DisplayName("EnquiryListRegularSearch - @/enquiry/search/regular - assignUser = Lati Test, status = in progress, " +
            "dateRange = 01/07/2018 - 31/07/2018")
    public void enquiryListControllerTest_4() throws Exception {

        searchWrapper.setAssignedUser("Lati Test");
        searchWrapper.setStatus("in progress");
        searchWrapper.setDateRange("01/07/2018 - 31/07/2018");

        this.mockMvc
                .perform(post("/enquiry/search/regular").with(csrf()).with(demo())
                        .flashAttr("searchWrapper", searchWrapper)
                        .param("searchFor", searchWrapper.getSearchFor())
                        .param("searchIn", searchWrapper.getSearchIn())
                        .param("limit", "" + searchWrapper.getLimit())
                        .param("dateRange", "01/07/2018 - 31/07/2018")  // value from the post form
                        .param("assignedUser", searchWrapper.getAssignedUser())
                        .param("closingUser", searchWrapper.getClosingUser())
                        .param("status", searchWrapper.getStatus())
                        .param("sortBy", "creation date") // value in post form
                        .param("direction", searchWrapper.getDirection())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryList"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("searchFor", Matchers.containsString(""))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("searchIn", Matchers.containsString("all"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("limit", Matchers.equalTo(0))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("dateRange", Matchers.containsString("01/07/2018 - 31/07/2018"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("assignedUser", Matchers.containsString("Lati Test"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("closingUser", Matchers.containsString("any user"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("status", Matchers.containsString("in progress"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("sortBy", Matchers.containsString("creation date"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("direction", Matchers.containsString("ascending"))))
                .andExpect(model().attribute("enquiryListWrapper",
                        Matchers.hasProperty( "enquiryList", Matchers
                                .hasSize(3)))); // 2 enquiries assign to Lati Test, in progress and within 01/07/2018 - 31/07/2018
    }

    @Test
    @DisplayName("EnquiryListRegularSearch - @/enquiry/search/regular - assignUser = Lati Test, status = in progress," +
            " dateRange = 01/07/2018 - 31/07/2018, customer name = Sarah Shopland")
    public void enquiryListControllerTest_5() throws Exception {

        searchWrapper.setAssignedUser("Lati Test");
        searchWrapper.setStatus("in progress");
        searchWrapper.setDateRange("01/07/2018 - 31/07/2018");
        searchWrapper.setSearchFor("Sarah Shopland");
        searchWrapper.setSearchIn("in customer names");

        this.mockMvc
                .perform(post("/enquiry/search/regular").with(csrf()).with(demo())
                        .flashAttr("searchWrapper", searchWrapper)
                        .param("searchFor", searchWrapper.getSearchFor())
                        .param("searchIn", searchWrapper.getSearchIn())
                        .param("limit", "" + searchWrapper.getLimit())
                        .param("dateRange", "01/07/2018 - 31/07/2018")  // value from the post form
                        .param("assignedUser", searchWrapper.getAssignedUser())
                        .param("closingUser", searchWrapper.getClosingUser())
                        .param("status", searchWrapper.getStatus())
                        .param("sortBy", "creation date") // value in post form
                        .param("direction", searchWrapper.getDirection())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryList"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("searchFor", Matchers.containsString("Sarah Shopland"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("searchIn", Matchers.containsString("in customer names"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("limit", Matchers.equalTo(0))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("dateRange", Matchers.containsString("01/07/2018 - 31/07/2018"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("assignedUser", Matchers.containsString("Lati Test"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("closingUser", Matchers.containsString("any user"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("status", Matchers.containsString("in progress"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("sortBy", Matchers.containsString("creation date"))))
                .andExpect(model().attribute("searchWrapper",
                        Matchers.hasProperty("direction", Matchers.containsString("ascending"))))
                .andExpect(model().attribute("enquiryListWrapper",
                        Matchers.hasProperty( "enquiryList", Matchers.hasSize(1)))) // 1 enquiry
                .andExpect(model().attribute("enquiryListWrapper", Matchers.hasProperty( "enquiryList", Matchers
                        .hasItem(Matchers.<Enquiry>hasProperty("name",
                                Matchers.equalToIgnoringCase("Sarah Shopland"))))));
    }

}














