package com.latidude99.integration;

import com.latidude99.model.Comment;
import com.latidude99.model.Enquiry;
import com.latidude99.model.User;
import com.latidude99.util.FormBean;
import com.latidude99.web.controller.EnquiryController;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.xml.transform.Result;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.latidude99.CustomSecurityMockMvcRequestPostProcessors.demo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/*
 *  - Mock server
 *  - @ActiveProfiles("test") - exclude AppConfig class that makes calls to
 *    database when starting the app and context wouldn't load with it
 *  - (secure=false) - to work around Spring Security authentication
 *  - tests Enquiry image property at http://localhost:8080/image/4 (test3)
 */

@Tag("slow")
@Tag("serverMock")
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "/test.properties")
@AutoConfigureMockMvc(secure=false)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class EnquiryControllerIntegrationTests {

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
                .andExpect(model().attribute("waiting", 6L))
                .andExpect(model().attribute("opened", 6L))
                .andExpect(model().attribute("closed", 6L))
                .andExpect(model().attribute("openedByUser", 4L))
                .andExpect(model().attribute("assignedToUserAndClosed", 6)) // declared int in the controller
                .andExpect(model().attribute("closedByUser", 6L))
                .andExpect(model().attribute("imageDbUrl", "http://localhost:8080/image/4"))
                .andExpect(model().attribute("email", nullValue()))
                .andExpect(model().attribute("emailFail", nullValue()))
                .andExpect(content().string(containsString("janros@hotmail.co.uk"))) // enquiry 4
                .andReturn();

    }

    @Test
    @DisplayName("EnquiryController - @/enquiry/comment - comment submition form")
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
                .andExpect(content().string(containsString("janros@hotmail.co.uk"))) // customer's email
                .andExpect(content().string(containsString("controller test @/enquiry/comment"))) // new comment
                .andReturn();

    }

    @Test
    @DisplayName("EnquiryController - @/enquiry/email - send an email")
    public void enquiryControllerTest_5() throws Exception {

        this.mockMvc
                .perform(post("/enquiry/email").with(csrf()).with(demo())
                        .param("id", "4")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryPage"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("email", "The enquiry has been successfully emailed to: "))
                .andExpect(model().attribute("emailFail", nullValue()))
                .andExpect(content().string(containsString("janros@hotmail.co.uk"))) // customer's email
                .andExpect(content().string(
                        containsString("The enquiry has been successfully emailed to: "))) // new comment
                .andReturn();

    }

    @Test
    @DisplayName("EnquiryController - @/enquiry/page/goto - go to enquiry, success")
    public void enquiryControllerTest_6() throws Exception {

        FormBean formBean = new FormBean();
        formBean.setNumber(4);

        MvcResult result = this.mockMvc
                .perform(post("/enquiry/page/goto").with(csrf()).with(demo())
                        .flashAttr("formBean", formBean)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryPage"))
                .andExpect(model().hasNoErrors())
                .andExpect(content().string(containsString("janros@hotmail.co.uk"))) // enquiry 4
                .andExpect(model().attribute("formBean", hasProperty("number", is(4))))
                .andReturn();

    }

    @Test
    @DisplayName("EnquiryController - @/enquiry/page/goto - go to enquiry, failure")
    public void enquiryControllerTest_7() throws Exception {

        FormBean formBean = new FormBean();
        formBean.setNumber(35); // no such enquiry

    this.mockMvc
                .perform(post("/enquiry/page/goto").with(csrf()).with(demo())
                        .flashAttr("formBean", formBean)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryPage"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("enquiry",
                        hasProperty("type",
                        is("---------------  THERE IS NO ENQUIRY NUMBER: " + 35 + "  ---------------"))));
    }



    @Test
    @DisplayName("EnquiryController - @/enquiry/page/next - next enquiry, success")
    public void enquiryControllerTest_8() throws Exception {
        Enquiry enquiry = new Enquiry();
        enquiry.setId(4L);

        this.mockMvc
                .perform(post("/enquiry/page/next").with(csrf()).with(demo())
                        .flashAttr("enquiry", enquiry)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryPage"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("enquiry",
                        hasProperty("id", is(5L))))
                .andExpect(model().attribute("enquiry",
                        hasProperty("email", is("kris.triggle@googlemail.com"))));

    }

    @Test
    @DisplayName("EnquiryController - @/enquiry/page/next - previous enquiry, failure")
    public void enquiryControllerTest_9() throws Exception {
        Enquiry enquiry = new Enquiry();
        enquiry.setId(1L);

        this.mockMvc
                .perform(post("/enquiry/page/previous").with(csrf()).with(demo())
                        .flashAttr("enquiry", enquiry)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryPage"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("enquiry",
                        hasProperty("type",
                        is("---------------  THERE IS NO ENQUIRY NUMBER: " + 0 + "  ---------------"))));
    }

    @Test
    @DisplayName("EnquiryController - @/enquiry/assign - assign enquiry to the current user")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void enquiryControllerTest_10() throws Exception {
        Enquiry enquiry = new Enquiry();
        enquiry.setId(5L);

        this.mockMvc
                .perform(post("/enquiry/assign").with(csrf()).with(demo())
                        .flashAttr("enquiry", enquiry)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryPage"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("waiting", 5L)) // -1
                .andExpect(model().attribute("opened", 7L)) // +1
                .andExpect(model().attribute("openedByUser", 5L)) // +1
                .andExpect(model().attribute("enquiry",
                        hasProperty("status", is("in progress"))))
                .andExpect(model().attribute("enquiry",
                        hasProperty("sortedProgressUsersWithDate",
                        hasItem(startsWith("Demo")))));
    }

    @Test
    @DisplayName("EnquiryController - @/enquiry/deassign - deassign enquiry from the current user")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void enquiryControllerTest_11() throws Exception {
        Enquiry enquiry = new Enquiry();
        enquiry.setId(9L);

        this.mockMvc
                .perform(post("/enquiry/deassign").with(csrf()).with(demo())
                        .flashAttr("enquiry", enquiry)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryPage"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("waiting", 6L)) // no change
                .andExpect(model().attribute("opened", 6L)) // no change
                .andExpect(model().attribute("openedByUser", 3L)) // -1
                .andExpect(model().attribute("enquiry",
                        hasProperty("status", is("in progress"))))
                .andExpect(model().attribute("enquiry",
                        hasProperty("sortedProgressUsersWithDate", hasSize(2))));
    }

    /*
     * @/enquiry/close -> "openedByUser" value decreases by 1
     */
    @Test
    @DisplayName("EnquiryController - @/enquiry/close - closes the enquiry")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void enquiryControllerTest_12() throws Exception {
        Enquiry enquiry = new Enquiry();
        enquiry.setId(9L);

        this.mockMvc
                .perform(post("/enquiry/close").with(csrf()).with(demo())
                        .flashAttr("enquiry", enquiry)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryPage"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("waiting", 6L)) // no change
                .andExpect(model().attribute("opened", 5L)) // -1
                .andExpect(model().attribute("openedByUser", 3L)) // -1
                .andExpect(model().attribute("closed", 7L)) // +1
                .andExpect(model().attribute("assignedToUserAndClosed", 7)) // +1
                .andExpect(model().attribute("enquiry",
                        hasProperty("status", is("closed"))))
                .andExpect(model().attribute("enquiry",
                        hasProperty("sortedProgressUsersWithDate", hasSize(3))))
                .andExpect(model().attribute("enquiry",
                        hasProperty("closingUser", hasToString(containsString("Demo")))));
    }

    /*
     * @/enquiry/open -> "openedByUser" value doesn't change because
     * open =/= assign and "openedByUser" really means "assign" to the user
     * (makes sense when one user opens an enquiry not assign to them and doesn't
     * want to be assign to it but only marked it as not finished)
     * Will consider refactoring the names here a bit.
     */
    @Test
    @DisplayName("EnquiryController - @/enquiry/open - opens the enquiry without assigning")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void enquiryControllerTest_13() throws Exception {
        Enquiry enquiry = new Enquiry();
        enquiry.setId(4L);

        this.mockMvc
                .perform(post("/enquiry/open").with(csrf()).with(demo())
                        .flashAttr("enquiry", enquiry)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryPage"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("waiting", 5L)) // -1
                .andExpect(model().attribute("opened", 7L)) // +1
                .andExpect(model().attribute("openedByUser", 4L)) // no change
                .andExpect(model().attribute("closed", 6L)) // +1
                .andExpect(model().attribute("assignedToUserAndClosed", 6)) // no change!
                .andExpect(model().attribute("enquiry",
                        hasProperty("status", is("in progress"))))
                .andExpect(model().attribute("enquiry",
                        hasProperty("sortedProgressUsersWithDate", hasSize(0)))); //no change!
    }


}



























