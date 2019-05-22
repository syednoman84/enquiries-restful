package com.latidude99.integration;

import com.latidude99.model.Enquiry;
import com.latidude99.web.controller.EnquiryController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.latidude99.CustomSecurityMockMvcRequestPostProcessors.demo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
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
 */

@Tag("slow")
@Tag("serverMock")
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "/test.properties")
@AutoConfigureMockMvc(secure=false)
public class EnquiryListControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EnquiryController enquiryListController;

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
    @DisplayName("EnquiryListController - isNotNull check")
    @WithMockUser(username = "Demo", password = "111111", roles = "USER")
    public void enquiryListControllerTest_0() {
        assertThat(enquiryListController).isNotNull();
    }

    @Test
    @DisplayName("EnquiryListController - response check")
//    @WithMockUser(username = "Demo", password = "111111", roles = "USER")
    public void enquiryListControllerTest_1() throws Exception {

        mockMvc.perform(post("/enquiry/list")
                .with(csrf())
                .with(demo()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @DisplayName("EnquiryListController - checks status and model attributes")
    public void enquiryListControllerTest_2() throws Exception {

        this.mockMvc
                .perform(get("/enquiry/list").with(demo()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryList"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attributeExists("formBean"))
                .andExpect(model().attributeExists("searchWrapper"))
                .andExpect(model().attributeExists("currentUser"))
                .andExpect(model().attributeExists("enquiryListWrapper"))
                .andExpect(model().attributeExists("waiting"))
                .andExpect(model().attributeExists("opened"))
                .andExpect(model().attributeExists("closed"))
                .andExpect(model().attributeExists("openedByUser"))
                .andExpect(model().attributeExists("assignedToUserAndClosed"))
                .andExpect(model().attributeExists("closedByUser"))
                .andExpect(content().string(containsString("kris.triggle@googlemail.com")))
                .andReturn();

    }
/*
    @Test
    @DisplayName("EnquiryListController - checks status and model attributes")
    @WithMockUser(username = "demo@demo.com", password = "111111", roles = "USER")
    public void enquiryControllerTest_1() throws Exception {
        Enquiry enquiry = new Enquiry();
        enquiry.setId(5L);

        this.mockMvc    // MockMvcRequestBuilderUtils.postForm("/enquiry/page", enquiry))
                .perform(post("/enquiry/page")
                        .param("id", "5")
                        .flashAttr("enquiry", enquiry))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryPage"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attributeExists("enquiry"))
                .andReturn();

    }
*/
}



/*     this.mvc
                .perform(get("/enquiry/page"))
                .andExpect(status().isOk())
                .andExpect(model().hasNoErrors())
                .andExpect(view().name("enquiryPage"))
                .andExpect(model().attributeExists("enquiry"))
                .andExpect(model().attributeExists("waiting"))
                .andExpect(model().attributeExists("opened"))
                .andExpect(model().attributeExists("closed"))
                .andExpect(model().attributeExists("openedByUser"))
                .andExpect(model().attributeExists("assignedToUserAndClosed"))
                .andExpect(model().attributeExists("closedByUser"))
                .andExpect(model().attributeExists("imageDbUrl"))
                .andExpect(model().attribute("email", null))
                .andExpect(model().attribute("emailFail", nullValue()))
                .andExpect(content().string(containsString("kris.triggle@googlemail.com"))); // enquiry 5
                */























