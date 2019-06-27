package com.latidude99.integration.rest;

import com.latidude99.model.User;
import com.latidude99.repository.UserRepository;
import com.latidude99.web.rest.UserControllerRest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.latidude99.CustomSecurityMockMvcRequestPostProcessors.demo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerRestIntegrationTests_MockMvc {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserControllerRest userControllerRest;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Order(1)
    @Test
    @DisplayName("UserController - isNotNull check")
    public void userControllerTest_1() {
        assertThat(userControllerRest).isNotNull();
    }


    @Test
    @DisplayName("UserController - @/api/user/update - checks user password change")
//   @WithMockUser(username = "demo@demo.com", password = "111111", roles = "USER")
    public void userControllerTest_2() throws Exception {

        mockMvc.perform(post("/api/user/update").with(csrf()).with(demo())
                .param("passwordOld", "111111")
                .param("passwordNew", "222222")
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$", Matchers.is("password changed")))
                ;

        User user = userRepository.findByEmail("demo@demo.com");
        assertTrue(passwordEncoder.matches("222222", user.getPassword()),
                "incorrect user password after change");
    }

    @Test
    @DisplayName("UserController - @/api/user/activate - user activation request with token")
    @Sql(scripts = "/user-controller-integration-test_3_add_activation_token.sql", executionPhase = BEFORE_TEST_METHOD)
    public void userControllerTest_3() throws Exception {

        String token = "testActivationTokenString";
        User testUser = userRepository.findByActivationToken(token);
        assertFalse(testUser.isEnabled());

        // failure
        mockMvc.perform(get("/api/user/activate").with(csrf())
                .param("activationToken", "incorrectTestActivationTokenString") // incorrect token value
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isExpectationFailed())
                .andExpect(jsonPath("$.status",
                        Matchers.is(HttpStatus.EXPECTATION_FAILED
                                .getReasonPhrase().toUpperCase().replace(' ', '_'))))
                .andExpect(jsonPath("$.message", Matchers.is("invalid activation token")))
                .andExpect(jsonPath("$.errors.[0]", Matchers.is("not authorized")))
        ;

        testUser = userRepository.findByEmail(testUser.getEmail());
        assertEquals(token, testUser.getActivationToken(),
                "incorrect token value after failed activation attempt");

        // success
        mockMvc.perform(get("/api/user/activate").with(csrf())
                .param("activationToken", token) // correct token value
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted())
                .andExpect(content().string("user " + testUser.getEmail() + " activated"))
        ;

        testUser = userRepository.findByEmail(testUser.getEmail());
        assertNull(testUser.getActivationToken(),
                "incorrect token value after successful activation");

    }

    @Test
    @DisplayName("UserController - @/api/user/reset - user password reset request with token")
    @Sql(scripts = "/user-controller-integration-test_4_add_reset_token.sql", executionPhase = BEFORE_TEST_METHOD)
    public void userControllerTest_4() throws Exception {

        String token = "testResetTokenString";
        User testUser = userRepository.findByResetToken(token);
        assertTrue(testUser.isEnabled());

        // failure
        mockMvc.perform(get("/api/user/reset") .with(csrf())
                .param("resetToken", "incorrectTestResetTokenString") // incorrect token value
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isExpectationFailed())
                .andExpect(jsonPath("$.status",
                        Matchers.is(HttpStatus.EXPECTATION_FAILED
                                .getReasonPhrase().toUpperCase().replace(' ', '_'))))
                .andExpect(jsonPath("$.message", Matchers.is("invalid reset token")))
                .andExpect(jsonPath("$.errors.[0]", Matchers.is("not authorized")))
        ;

        testUser = userRepository.findByEmail(testUser.getEmail());
        assertEquals(token, testUser.getResetToken(),
                "incorrect token value after failed reset attempt");


        //success
        mockMvc.perform(get("/api/user/reset").with(csrf())
                .param("resetToken", token) // correct token value
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted())
                .andExpect(content().string(token))
        ;

        testUser = userRepository.findByEmail(testUser.getEmail());
        assertEquals(token, testUser.getResetToken(),
                "incorrect token value after successful reset attempt");
    }

    @Test
    @DisplayName("UserController - @/api/user/reset - user password reset/new")
    @Sql(scripts = "/user-controller-integration-test_5_add_user.sql", executionPhase = BEFORE_TEST_METHOD)
    public void userControllerTest_5() throws Exception {

        // user/password check before (PasswordEncoder not used when @Sql script used)
        String token = "testResetTokenString";
        User user = userRepository.findByResetToken(token);

        // failure
        mockMvc.perform(post("/api/user/reset") .with(csrf())
                .param("token", "incorrectTestResetTokenString") // incorrect token value
                .param("password", "new reset password")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isExpectationFailed())
                .andExpect(jsonPath("$.status",
                        Matchers.is(HttpStatus.EXPECTATION_FAILED
                                .getReasonPhrase().toUpperCase().replace(' ', '_'))))
                .andExpect(jsonPath("$.message", Matchers.is("invalid reset token")))
                .andExpect(jsonPath("$.errors.[0]", Matchers.is("not authorized")))
        ;

        user = userRepository.findByResetToken(token);
        assertEquals("old_password_reset", user.getPassword(),
                "incorrect password after failed change attempt");

        // success
        mockMvc.perform(post("/api/user/reset").with(csrf())
                .param("token", token) // correct token value
                .param("password", "new reset password")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", Matchers.is(user.getEmail())))
                .andExpect(jsonPath("$.resetToken", Matchers.nullValue()))
        ;

        // confirms password change
        user = userRepository.findByEmail("test_user_reset_form@test.com");
        assertTrue(passwordEncoder.matches("new reset password", user.getPassword()),
        "incorrect password after change");

    }

    @Test
    @DisplayName("UserController - @/api/user/forgot - user password forgot form")
    @Sql(scripts = "/user-controller-integration-test_6_add_user.sql", executionPhase = BEFORE_TEST_METHOD)
    public void userControllerTest_6() throws Exception {
        String incorrectEmail = "wrong_user_forgot@test.com";

        // user/password check before (PasswordEncoder not used when @Sql script used)
        User user = userRepository.findByEmail("test_user_forgot@test.com");
        assertEquals("emptyResetToken", user.getResetToken());

        // failure
        mockMvc.perform(post("/api/user/forgot").with(csrf()).with(demo())
                .param("email", incorrectEmail) // non-existent user
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status",
                        Matchers.is(HttpStatus.NOT_FOUND
                                .getReasonPhrase().toUpperCase().replace(' ', '_'))))
                .andExpect(jsonPath("$.message",
                        Matchers.is("no register user with the email address: " + incorrectEmail)))
                .andExpect(jsonPath("$.errors.[0]", Matchers.is("resource not found")))
        ;

        user = userRepository.findByEmail("test_user_forgot@test.com");
        assertEquals("emptyResetToken", user.getResetToken());


        // success
        mockMvc.perform(post("/api/user/forgot").with(csrf()).with(demo())
                .param("email", "test_user_forgot@test.com") // existent user
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", Matchers.is(user.getEmail())))
                .andExpect(jsonPath("$.resetToken", Matchers.not("emptyResetToken")))
        ;

        user = userRepository.findByEmail("test_user_forgot@test.com");
        assertNotEquals("emptyResetToken", user.getResetToken());
    }

}















