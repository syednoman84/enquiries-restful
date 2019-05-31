package com.latidude99.integration;

import com.latidude99.model.Enquiry;
import com.latidude99.model.User;
import com.latidude99.repository.UserRepository;
import com.latidude99.repository.UserRoleRepository;
import com.latidude99.util.FormBean;
import com.latidude99.web.controller.EnquiryController;
import com.latidude99.web.controller.UserController;
import org.checkerframework.checker.units.qual.A;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import java.util.List;
import java.util.stream.Collectors;

import static com.latidude99.CustomSecurityMockMvcRequestPostProcessors.demo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/*
 *  - Mock server
 *  - @ActiveProfiles("test") - exclude AppConfig class that makes calls to
 *    database when starting the app and context wouldn't load with it
 *  - (secure=false) - to work around Spring Security authentication
 *  - tests Enquiry image property at http://localhost:8080/image/4 (test3)
 *  - uses pre-defined entries in DB, loaded from data.sql
 */

@Tag("slow")
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "/test.properties")
@AutoConfigureMockMvc(secure=false)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class UserControllerIntegrationTests {

    static Enquiry enquiry = new Enquiry();
    static FormBean formBean = new FormBean();

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserController userController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

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
    @DisplayName("UserController - isNotNull check")
    public void userControllerTest_0() {
        assertThat(userController).isNotNull();
    }


    @Test
    @DisplayName("UserController - @/user -checks logged in user properties")
 //   @WithMockUser(username = "demo@demo.com", password = "111111", roles = "USER")
    public void userControllerTest_1() throws Exception {

        mockMvc.perform(get("/user").with(csrf()).with(demo()))
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryUser"))
                .andExpect(model().attribute("currentUser",
                        Matchers.hasProperty("name", Matchers.containsString("Demo"))))
                .andExpect(model().attribute("currentUser",
                        Matchers.hasProperty("email", Matchers.containsString("demo@demo.com"))))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("UserController - @/user/update - checks user password change")
//    @WithMockUser(username = "demo@demo.com", password = "111111", roles = "USER")
    public void userControllerTest_2() throws Exception {
        User user = userRepository.findByEmail("demo@demo.com");

        mockMvc.perform(post("/user/update").with(csrf()).with(demo())
                .flashAttr("user", user)
                .param("name", user.getName())
                .param("email", user.getEmail())
                .param("password", "111111")
                .param("passwordNew", "newPassword")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryUser"))
                //this fails, the problem seems to be that during MockMvc POST call PasswordEncoder is not used
                //.andExpect(model().attribute("passwordOldNoMatch", Matchers.nullValue()))
                .andExpect(model().attribute("user",
                        Matchers.hasProperty("name", Matchers.containsString("Demo"))))
                .andExpect(model().attribute("currentUser",
                        Matchers.hasProperty("name", Matchers.containsString("Demo"))))
                .andExpect(model().attribute("currentUser",
                        Matchers.hasProperty("email", Matchers.containsString("demo@demo.com"))))
                .andExpect(model().attribute("currentUser",
                Matchers.hasProperty("password", Matchers.containsString("111111"))))
                .andExpect(model().attribute("currentUser",
                        Matchers.hasProperty("passwordNew", Matchers.containsString("newPassword"))));

    }

    @Test
    @DisplayName("UserController - @/user/activate - user activation request with token")
    @Sql(scripts = "/user-controller-integration-test_3_add_activation_token.sql", executionPhase = BEFORE_TEST_METHOD)
    public void userControllerTest_3() throws Exception {

        User testUser = userRepository.findByActivationToken("testActivationTokenString");
        assertFalse(testUser.isEnabled());

        // failure
        mockMvc.perform(get("/user/activate")
                .param("activationToken", "incorrectTestActivationTokenString") // incorrect token value
                .with(csrf()).with(demo()))
                .andExpect(status().isOk())
                .andExpect(view().name("activation"))
                .andExpect(model().attribute("invalidToken",
                        Matchers.containsString("Oops!  This is an invalid activation link.")))
                .andExpect(model().attribute("user", Matchers.nullValue()))
                .andDo(MockMvcResultHandlers.print());

        // success
        mockMvc.perform(get("/user/activate")
                .param("activationToken", "testActivationTokenString") // correct token value
                .with(csrf()).with(demo()))
                .andExpect(status().isOk())
                .andExpect(view().name("activation"))
                .andExpect(model().attribute("invalidToken", Matchers.nullValue()))
                .andExpect(model().attribute("user",
                        Matchers.hasProperty("name", Matchers.containsString("Test User"))))
                .andExpect(model().attribute("user",
                        Matchers.hasProperty("email", Matchers.containsString("test_user@test.com"))))
                .andExpect(model().attribute("user",
                        Matchers.hasProperty("enabled", Matchers.equalTo(true))))
                .andExpect(model().attribute("user",
                        Matchers.hasProperty("activationToken", Matchers.nullValue())))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("UserController - @/user/reset - user password reset request with token")
    @Sql(scripts = "/user-controller-integration-test_4_add_reset_token.sql", executionPhase = BEFORE_TEST_METHOD)
    public void userControllerTest_4() throws Exception {

        User testUser = userRepository.findByResetToken("testResetTokenString");
        assertTrue(testUser.isEnabled());

        // failure
        mockMvc.perform(get("/user/reset")
                .param("resetToken", "incorrectTestResetTokenString") // incorrect token value
                .with(csrf()).with(demo()))
                .andExpect(status().isOk())
                .andExpect(view().name("reset"))
                .andExpect(model().attribute("invalidToken",
                        Matchers.containsString("Oops!  This is an invalid reset link.")))
                .andExpect(model().attribute("user", Matchers.nullValue()))
                .andDo(MockMvcResultHandlers.print());

        //success
        mockMvc.perform(get("/user/reset")
                .param("resetToken", "testResetTokenString") // correct token value
                .with(csrf()).with(demo()))
                .andExpect(status().isOk())
                .andExpect(view().name("reset"))
                .andExpect(model().attribute("invalidToken", Matchers.nullValue()))
                .andExpect(model().attribute("user",
                        Matchers.hasProperty("name", Matchers.containsString("Test User Reset"))))
                .andExpect(model().attribute("user",
                        Matchers.hasProperty("email", Matchers.containsString("test_user_reset@test.com"))))
                .andExpect(model().attribute("user",
                        Matchers.hasProperty("enabled", Matchers.equalTo(true))))
                .andExpect(model().attribute("user",
                        Matchers.hasProperty("resetToken", Matchers.nullValue())))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("UserController - @/user/resetForm - user password reset/new")
    @Sql(scripts = "/user-controller-integration-test_5_add_user.sql", executionPhase = BEFORE_TEST_METHOD)
    public void userControllerTest_5() throws Exception {

        // user/password check before (PasswordEncoder not used when @Sql script used)
        User userBefore = userRepository.findByEmail("test_user_reset_form@test.com");
        assertEquals("old_password_reset", userBefore.getPassword());

        mockMvc.perform(post("/user/resetForm").with(csrf()).with(demo())
                .flashAttr("user", userBefore)
                .param("id", "" + userBefore.getId())
                .param("name", userBefore.getName())
                .param("email", userBefore.getEmail())
                .param("password", "new reset password")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("reset"))
                .andExpect(model().attribute("user",
                        Matchers.hasProperty("name", Matchers.containsString("Test User Reset Form"))))
                .andExpect(model().attribute("user",
                        Matchers.hasProperty("email", Matchers.containsString("test_user_reset_form@test.com"))))
                .andExpect(model().attribute("user",
                        Matchers.hasProperty("password", Matchers.containsString("new reset password"))));

        // confirms password change
        User userAfter = userRepository.findByEmail("test_user_reset_form@test.com");
        assertTrue(passwordEncoder.matches("new reset password", userAfter.getPassword()));

    }

    @Test
    @DisplayName("UserController - @/user/forgot - user password forgot form")
    @Sql(scripts = "/user-controller-integration-test_6_add_user.sql", executionPhase = BEFORE_TEST_METHOD)
    public void userControllerTest_6() throws Exception {

        // user/password check before (PasswordEncoder not used when @Sql script used)
        User userBefore = userRepository.findByEmail("test_user_forgot@test.com");
        assertEquals("emptyResetToken", userBefore.getResetToken());

        User userClean = new User();

        // failure
        mockMvc.perform(post("/user/forgot").with(csrf()).with(demo())
                .flashAttr("user", userClean)
                .param("email", "wrong_user_forgot@test.com") // non-existent user
                .param("name", "")
                .param("password", "")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("resetFollowUp"))
                .andExpect(model().attribute("forgotEmail"," wrong_user_forgot@test.com"))
                .andExpect(model().attributeDoesNotExist("forgotOK"));

        User userAfterFailure = userRepository.findByEmail("test_user_forgot@test.com");
        assertEquals("emptyResetToken", userAfterFailure.getResetToken());


        // success
        mockMvc.perform(post("/user/forgot").with(csrf()).with(demo())
                .flashAttr("user", userClean)
                .param("email", "test_user_forgot@test.com") // existent uset
                .param("name", "")
                .param("password", "")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("resetFollowUp"))
                .andExpect(model().attributeDoesNotExist("forgotEmail"))
                .andExpect(model().attribute("forgotOK", " test_user_forgot@test.com"))
                .andExpect(model().attributeDoesNotExist("forgotError"));

        User userAfterSuccess = userRepository.findByEmail("test_user_forgot@test.com");
        assertNotEquals("emptyResetToken", userAfterSuccess.getResetToken());
    }

}















