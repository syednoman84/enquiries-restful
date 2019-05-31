package com.latidude99.integration;

import com.latidude99.model.Enquiry;
import com.latidude99.model.Role;
import com.latidude99.model.User;
import com.latidude99.model.UserRole;
import com.latidude99.repository.UserRepository;
import com.latidude99.repository.UserRoleRepository;
import com.latidude99.service.UserRoleService;
import com.latidude99.service.UserService;
import com.latidude99.util.FormBean;
import com.latidude99.web.controller.AdminController;
import com.latidude99.web.controller.UserController;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.validation.constraints.AssertTrue;

import java.util.List;
import java.util.stream.Collectors;

import static com.latidude99.CustomSecurityMockMvcRequestPostProcessors.demo;
import static com.latidude99.CustomSecurityMockMvcRequestPostProcessors.latiDude;
import static com.latidude99.CustomSecurityMockMvcRequestPostProcessors.latiTest;
import static java.lang.Enum.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "/test.properties")
@AutoConfigureMockMvc(secure=false)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AdminControllerIntegrationTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AdminController adminController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    UserRoleRepository roleRepository;

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
    @DisplayName("AdminController - isNotNull check")
    public void adminControllerTest_0() {
        assertThat(adminController).isNotNull();
    }


    @Test
    @DisplayName("AdminController - @/admin -checks logged in user/admin properties")
 //   @WithMockUser(username = "latidude99@gmail.com", password = "0011100", roles = "APPADMIN") // latiDude
 //   @WithMockUser(username = "latidude99test@gmail.com", password = "1100011", roles = "ADMIN") //latiTest
 //   @Sql(scripts = "/admin-controller-integration-test_2_add_user.sql", executionPhase = BEFORE_TEST_METHOD)
    public void adminControllerTest_1() throws Exception {

        mockMvc.perform(get("/admin").with(csrf()).with(demo())) // demo = USER
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryUser"))
                .andExpect(model().attribute("currentUser",
                        Matchers.hasProperty("name", Matchers.containsString("Demo"))))
                .andExpect(model().attribute("currentUser",
                        Matchers.hasProperty("email", Matchers.containsString("demo@demo.com"))))
                .andExpect(model().attribute("currentUser",
                        Matchers.hasProperty("roles", Matchers.hasItem(Matchers.<UserRole>hasProperty(
                                "role", Matchers.containsString("ROLE_USER"))))))
                .andDo(MockMvcResultHandlers.print());

        mockMvc.perform(get("/admin").with(csrf()).with(latiDude())) // latiDude = APPADMIN
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryAdmin"))
                .andExpect(model().attribute("currentUser",
                        Matchers.hasProperty("name", Matchers.containsString("Lati Dude"))))
                .andExpect(model().attribute("currentUser",
                        Matchers.hasProperty("email", Matchers.containsString("latidude99@gmail.com"))))
                .andExpect(model().attribute("currentUser",
                        Matchers.hasProperty("roles", Matchers.hasItem(Matchers.<UserRole>hasProperty(
                                "role", Matchers.containsString("ROLE_APPADMIN"))))))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("AdminController - @/user/enable - enable/disable users as ADMIN")
    public void adminControllerTest_2() throws Exception {

        User userToUpdateUSER = userRepository.findByEmail("demo@demo.com");
        assertTrue(userToUpdateUSER.isEnabled(), "test user not enabled"); // USER enabled

        mockMvc.perform(post("/user/enable").with(csrf()).with(latiTest())
                .flashAttr("user", userToUpdateUSER)
                .param("id", "" + userToUpdateUSER.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryAdmin"))
                .andExpect(model().attribute("currentUser", // logged in user with ADMIN privileges
                        Matchers.hasProperty("roles", Matchers.hasItem(Matchers.<UserRole>hasProperty(
                                "role", Matchers.containsString("ROLE_ADMIN"))))))
                .andExpect(model().attribute("users",
                        Matchers.hasItem(Matchers.<User>hasProperty("email",
                                Matchers.equalToIgnoringCase("demo@demo.com")))));

        // after disabling
        assertFalse(userRepository.findByEmail("demo@demo.com").isEnabled(), "test USER not disabled");

        mockMvc.perform(post("/user/enable").with(csrf()).with(latiTest()) // latiTest = ADMIN
                .flashAttr("user", userToUpdateUSER)
                .param("id", "" + userToUpdateUSER.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryAdmin"))
                .andExpect(model().attribute("currentUser", // logged in user with ADMIN privileges
                        Matchers.hasProperty("roles", Matchers.hasItem(Matchers.<UserRole>hasProperty(
                                "role", Matchers.containsString("ROLE_ADMIN"))))))
                .andExpect(model().attribute("users",
                        Matchers.hasItem(Matchers.<User>hasProperty("email",
                                Matchers.equalToIgnoringCase("demo@demo.com")))));

        // after enabling again
        assertTrue(userRepository.findByEmail("demo@demo.com").isEnabled(), "test USER not enabled");
    }

    @Test
    @DisplayName("AdminController - @/user/enable - enable/disable users as USER")
    public void adminControllerTest_3() throws Exception {

        User userToUpdateADMIN = userRepository.findByEmail("latidude99test@gmail.com");
        assertTrue(userToUpdateADMIN.isEnabled(), "test user not enabled"); // ADMIN enabled

        mockMvc.perform(post("/user/enable").with(csrf()).with(demo()) // demo() = USER
                .flashAttr("user", userToUpdateADMIN)
                .param("id", "" + userToUpdateADMIN.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryAdmin"))
                .andExpect(model().attribute("currentUser", // logged in user with USER privileges
                        Matchers.hasProperty("roles", Matchers.hasItem(Matchers.<UserRole>hasProperty(
                                "role", Matchers.containsString("ROLE_USER"))))))
                .andExpect(model().attribute("users",
                        Matchers.hasItem(Matchers.<User>hasProperty("email",
                                Matchers.equalToIgnoringCase("latidude99test@gmail.com")))));

        // after disabling attempt
        assertTrue(userRepository.findByEmail("latidude99test@gmail.com").isEnabled(), "test ADMIN disabled");
    }

    @Test
    @DisplayName("AdminController - @/user/block - block/unblock users as ADMIN")
    public void adminControllerTest_4() throws Exception {

        User userToUpdateUSER = userRepository.findByEmail("demo@demo.com");
        assertFalse(userToUpdateUSER.isBlocked(), "test user blocked"); // USER not blocked

        mockMvc.perform(post("/user/block").with(csrf()).with(latiTest())
                .flashAttr("user", userToUpdateUSER)
                .param("id", "" + userToUpdateUSER.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryAdmin"))
                .andExpect(model().attribute("currentUser", // logged in user with ADMIN privileges
                        Matchers.hasProperty("roles", Matchers.hasItem(Matchers.<UserRole>hasProperty(
                                "role", Matchers.containsString("ROLE_ADMIN"))))))
                .andExpect(model().attribute("users",
                        Matchers.hasItem(Matchers.<User>hasProperty("email",
                                Matchers.equalToIgnoringCase("demo@demo.com")))));

        // after blocking
        assertTrue(userRepository.findByEmail("demo@demo.com").isBlocked(), "test USER not blocked");

        mockMvc.perform(post("/user/block").with(csrf()).with(latiTest()) // latiTest = ADMIN
                .flashAttr("user", userToUpdateUSER)
                .param("id", "" + userToUpdateUSER.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryAdmin"))
                .andExpect(model().attribute("currentUser", // logged in user with ADMIN privileges
                        Matchers.hasProperty("roles", Matchers.hasItem(Matchers.<UserRole>hasProperty(
                                "role", Matchers.containsString("ROLE_ADMIN"))))))
                .andExpect(model().attribute("users",
                        Matchers.hasItem(Matchers.<User>hasProperty("email",
                                Matchers.equalToIgnoringCase("demo@demo.com")))));

        // after unblocking
        assertFalse(userRepository.findByEmail("demo@demo.com").isBlocked(), "test USER not enabled");
    }

    @Test
    @DisplayName("AdminController - @/user/block - block/unblock users as USER")
    public void adminControllerTest_5() throws Exception {

        User userToUpdateADMIN = userRepository.findByEmail("latidude99test@gmail.com");
        assertFalse(userToUpdateADMIN.isBlocked(), "test user blocked"); // ADMIN not blocked

        mockMvc.perform(post("/user/block").with(csrf()).with(demo()) // demo() = USER
                .flashAttr("user", userToUpdateADMIN)
                .param("id", "" + userToUpdateADMIN.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryAdmin"))
                .andExpect(model().attribute("currentUser", // logged in user with USER privileges
                        Matchers.hasProperty("roles", Matchers.hasItem(Matchers.<UserRole>hasProperty(
                                "role", Matchers.containsString("ROLE_USER"))))))
                .andExpect(model().attribute("users",
                        Matchers.hasItem(Matchers.<User>hasProperty("email",
                                Matchers.equalToIgnoringCase("latidude99test@gmail.com")))));

        // after blocking attempt
        assertFalse(userRepository.findByEmail("latidude99test@gmail.com").isBlocked(), "test ADMIN blocked");
    }

    @Test
    @DisplayName("AdminController - @/user/privileges - change USER to ADMIN to USER, by ADMIN")
    public void adminControllerTest_7() throws Exception {

        User userToUpdateUSER = userRepository.findByEmail("demo@demo.com");
        List<UserRole> roleList = userToUpdateUSER.getRoles().stream().collect(Collectors.toList());
        assertEquals(Role.DEFAULT.getText(), roleList.get(0).getRole(),"userToUpdateUSER not USER");

        mockMvc.perform(post("/user/privileges").with(csrf()).with(latiTest()) // latiTest() = ADMIN
                .flashAttr("user", userToUpdateUSER)
                .param("id", "" + userToUpdateUSER.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryAdmin"))
                .andExpect(model().attribute("currentUser", // logged in user with ADMIN privileges
                        Matchers.hasProperty("roles", Matchers.hasItem(Matchers.<UserRole>hasProperty(
                                "role", Matchers.containsString("ROLE_ADMIN"))))));

        // after changing to ADMIN
        userToUpdateUSER = userRepository.findByEmail("demo@demo.com");
        roleList = userToUpdateUSER.getRoles().stream().collect(Collectors.toList());
        assertEquals(Role.ADMIN.getText(), roleList.get(0).getRole(),"userToUpdateUSER not ADMIN");

        mockMvc.perform(post("/user/privileges").with(csrf()).with(latiTest())
                .flashAttr("user", userToUpdateUSER)
                .param("id", "" + userToUpdateUSER.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryAdmin"))
                .andExpect(model().attribute("currentUser", // logged in user with ADMIN privileges
                        Matchers.hasProperty("roles", Matchers.hasItem(Matchers.<UserRole>hasProperty(
                                "role", Matchers.containsString("ROLE_ADMIN"))))));

        // after changing back to USER
        userToUpdateUSER = userRepository.findByEmail("demo@demo.com");
        roleList = userToUpdateUSER.getRoles().stream().collect(Collectors.toList());
        assertEquals(Role.DEFAULT.getText(), roleList.get(0).getRole(),"userToUpdateUSER not USER");
    }

    @Test
    @DisplayName("AdminController - @/user/privileges - change ADMIN to USER to ADMIN, by APPADMIN")
    public void adminControllerTest_8() throws Exception {

        User userToUpdateADMIN = userRepository.findByEmail("latidude99test@gmail.com");
        List<UserRole> roleList = userToUpdateADMIN.getRoles().stream().collect(Collectors.toList());
        assertEquals(Role.ADMIN.getText(), roleList.get(0).getRole(),"userToUpdateADMIN not ADMIN");

        mockMvc.perform(post("/user/privileges").with(csrf()).with(latiDude())
                .flashAttr("user", userToUpdateADMIN)
                .param("id", "" + userToUpdateADMIN.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryAdmin"))
                .andExpect(model().attribute("currentUser", // logged in user with APPADMIN privileges
                        Matchers.hasProperty("roles", Matchers.hasItem(Matchers.<UserRole>hasProperty(
                                "role", Matchers.containsString("ROLE_APPADMIN"))))));

        // after changing to USER
        userToUpdateADMIN = userRepository.findByEmail("latidude99test@gmail.com");
        roleList = userToUpdateADMIN.getRoles().stream().collect(Collectors.toList());
        assertEquals(Role.DEFAULT.getText(), roleList.get(0).getRole(),"userToUpdateADMIN not USER");

        mockMvc.perform(post("/user/privileges").with(csrf()).with(latiDude()) // latiDude() = APPADMIN
                .flashAttr("user", userToUpdateADMIN)
                .param("id", "" + userToUpdateADMIN.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryAdmin"))
                .andExpect(model().attribute("currentUser", // logged in user with APPADMIN privileges
                        Matchers.hasProperty("roles", Matchers.hasItem(Matchers.<UserRole>hasProperty(
                                "role", Matchers.containsString("ROLE_APPADMIN"))))));

        // after changing back to ADMIN
        userToUpdateADMIN = userRepository.findByEmail("latidude99test@gmail.com");
        roleList = userToUpdateADMIN.getRoles().stream().collect(Collectors.toList());
        assertEquals(Role.ADMIN.getText(), roleList.get(0).getRole(),"userToUpdateADMIN not ADMIN");
    }

    @Test
    @DisplayName("AdminController - @/user/privileges - change APPADMIN to ADMIN, by ADMIN")
    public void adminControllerTest_9() throws Exception {

        User userToUpdateAPPADMIN = userRepository.findByEmail("latidude99@gmail.com");
        List<UserRole> roleList = userToUpdateAPPADMIN.getRoles().stream().collect(Collectors.toList());
        assertEquals(Role.APPADMIN.getText(), roleList.get(0).getRole(),"userToUpdateAPPADMIN not APPADMIN");

        mockMvc.perform(post("/user/privileges").with(csrf()).with(latiTest()) // latiTest() = ADMIN
                .flashAttr("user", userToUpdateAPPADMIN)
                .param("id", "" + userToUpdateAPPADMIN.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryAdmin"))
                .andExpect(model().attribute("currentUser", // logged in user with ADMIN privileges
                        Matchers.hasProperty("roles", Matchers.hasItem(Matchers.<UserRole>hasProperty(
                                "role", Matchers.containsString("ROLE_ADMIN"))))));

        // after trying to change to APPADMIN
        userToUpdateAPPADMIN = userRepository.findByEmail("latidude99@gmail.com");
        roleList = userToUpdateAPPADMIN.getRoles().stream().collect(Collectors.toList());
        assertNotEquals(Role.ADMIN.getText(), roleList.get(0).getRole(),"userToResetUSER is USER");
    }

    @Test
    @DisplayName("AdminController - @/user/reset - reset User pswd and sends pswd reset link, by ADMIN ")
    public void adminControllerTest_10() throws Exception {

        User userToResetUSER = userRepository.findByEmail("demo@demo.com");
        List<UserRole> roleList = userToResetUSER.getRoles().stream().collect(Collectors.toList());
        assertEquals(Role.DEFAULT.getText(), roleList.get(0).getRole(),"userToResetUSER not USER");
        assertNull(userToResetUSER.getResetToken(), "reset token not null");

        mockMvc.perform(post("/user/reset").with(csrf()).with(latiTest()) // latiTest() = ADMIN
                .flashAttr("user", userToResetUSER)
                .param("id", "" + userToResetUSER.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryAdmin"))
                .andExpect(model().attribute("emailResetError", nullValue()))
                .andExpect(model().attribute("emailResetOK", Matchers.hasProperty("email",
                        Matchers.containsString(userToResetUSER.getEmail()))))
                .andExpect(model().attribute("currentUser", // logged in user with ADMIN privileges
                        Matchers.hasProperty("roles", Matchers.hasItem(Matchers.<UserRole>hasProperty(
                                "role", Matchers.containsString("ROLE_ADMIN"))))));

        // after reset, USER
        userToResetUSER = userRepository.findByEmail("demo@demo.com");
        assertNotNull(userToResetUSER.getResetToken(), "reset token null");
    }

    @Test
    @DisplayName("AdminController - @/user/reset - resets ADMINAPP pswd and sends pswd reset link, by ADMIN")
    public void adminControllerTest_11() throws Exception {

        User userToResetAPPADMIN = userRepository.findByEmail("latidude99@gmail.com");
        List<UserRole> roleList = userToResetAPPADMIN.getRoles().stream().collect(Collectors.toList());
        assertEquals(Role.APPADMIN.getText(), roleList.get(0).getRole(),"userToResetUSER not USER");
        assertNull(userToResetAPPADMIN.getResetToken(), "reset token not null");

        mockMvc.perform(post("/user/reset").with(csrf()).with(latiTest()) // latiTest() = ADMIN
                .flashAttr("user", userToResetAPPADMIN)
                .param("id", "" + userToResetAPPADMIN.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryAdmin"))
                .andExpect(model().attributeDoesNotExist("emailResetError"))
                .andExpect(model().attributeDoesNotExist("emailResetOK"))
                .andExpect(model().attribute("admin", "admin"))
                .andExpect(model().attribute("currentUser", // logged in user with ADMIN privileges
                        Matchers.hasProperty("roles", Matchers.hasItem(Matchers.<UserRole>hasProperty(
                                "role", Matchers.containsString("ROLE_ADMIN"))))));

        // after reset attempt, APPADMIN
        userToResetAPPADMIN = userRepository.findByEmail("latidude99@gmail.com");
        assertNull(userToResetAPPADMIN.getResetToken(), "reset token null");
    }

    @Test
    @DisplayName("AdminController - @/user/activate - sends activation link to USER, by ADMIN")
    public void adminControllerTest_12() throws Exception {

        User userToActivateUSER = userRepository.findByEmail("demo@demo.com");
        List<UserRole> roleList = userToActivateUSER.getRoles().stream().collect(Collectors.toList());
        assertEquals(Role.DEFAULT.getText(), roleList.get(0).getRole(),"userToResetUSER not USER");
        assertNull(userToActivateUSER.getActivationToken(), "activation token not null");

        mockMvc.perform(post("/user/activate").with(csrf()).with(latiTest()) // latiTest() = ADMIN
                .flashAttr("user", userToActivateUSER)
                .param("id", "" + userToActivateUSER.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryAdmin"))
                .andExpect(model().attribute("privileges", nullValue()))
                .andExpect(model().attribute("emailResetError", nullValue()))
                .andExpect(model().attribute("emailActivationOK", Matchers.hasProperty("activationToken",
                        Matchers.notNullValue())))
                .andExpect(model().attributeDoesNotExist("emailActivationError"))
                .andExpect(model().attribute("currentUser", // logged in user with ADMIN privileges
                        Matchers.hasProperty("roles", Matchers.hasItem(Matchers.<UserRole>hasProperty(
                                "role", Matchers.containsString("ROLE_ADMIN"))))));

        // after activation, USER
        userToActivateUSER = userRepository.findByEmail("demo@demo.com");
        assertNotNull(userToActivateUSER.getActivationToken(), "activation token null");
    }

    @Test
    @DisplayName("AdminController - @/user/activate - sends activation link to APPADMIN, by ADMIN")
    public void adminControllerTest_13() throws Exception {

        User userToActivateAPPADMIN = userRepository.findByEmail("latidude99@gmail.com");
        List<UserRole> roleList = userToActivateAPPADMIN.getRoles().stream().collect(Collectors.toList());
        assertEquals(Role.APPADMIN.getText(), roleList.get(0).getRole(),"userToResetUSER not USER");
        assertNull(userToActivateAPPADMIN.getActivationToken(), "activation token not null");

        mockMvc.perform(post("/user/activate").with(csrf()).with(latiTest()) // latiTest() = ADMIN
                .flashAttr("user", userToActivateAPPADMIN)
                .param("id", "" + userToActivateAPPADMIN.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryAdmin"))
                .andExpect(model().attribute("privileges", nullValue()))
                .andExpect(model().attribute("admin", "admin"))
                .andExpect(model().attributeDoesNotExist("emailResetError"))
                .andExpect(model().attributeDoesNotExist("emailActivationOK"))
                .andExpect(model().attributeDoesNotExist("emailActivationError"))
                .andExpect(model().attribute("currentUser", // logged in user with ADMIN privileges
                        Matchers.hasProperty("roles", Matchers.hasItem(Matchers.<UserRole>hasProperty(
                                "role", Matchers.containsString("ROLE_ADMIN"))))));

        // after activation, USER
        userToActivateAPPADMIN = userRepository.findByEmail("latidude99@gmail.com");
        assertNull(userToActivateAPPADMIN.getActivationToken(), "activation token not null");
    }

    @Test
    @DisplayName("AdminController - @/user/add - displays add user form")
    public void adminControllerTest_14() throws Exception {

        mockMvc.perform(get("/user/add").with(csrf()).with(latiTest())) // latiTest() = ADMIN
                .andExpect(status().isOk())
                .andExpect(view().name("addUser"))
                .andExpect(model().attribute("userNew", Matchers.hasProperty("password",
                        Matchers.containsString("not_important"))))
                .andExpect(model().attribute("addDefaultOK", nullValue()))
                .andExpect(model().attribute("addAdminOK", nullValue()))
                .andExpect(model().attribute("addError", nullValue()))
                .andExpect(model().attribute("nameTaken", nullValue()))
                .andExpect(model().attribute("emailTaken", nullValue()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("AdminController - @/user/add/defaultRole - creates USER, by ADMIN - success")
    public void adminControllerTest_15() throws Exception {

        UserRole defaultRole = roleRepository.findByRole(Role.DEFAULT.getText());

        User userNewUSER = new User();
        userNewUSER.setPassword("not_important");

        mockMvc.perform(post("/user/add/defaultRole").with(csrf()).with(latiTest()) // latiTest() = ADMIN
                .flashAttr("user", userNewUSER)
                .param("name", "New User")
                .param("email", "newUser@test.com")
                .param("password", "not_important")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("addUser"))
                .andExpect(model().attributeDoesNotExist("nameTaken"))
                .andExpect(model().attributeDoesNotExist("emailTaken"))
                .andExpect(model().attribute("userNew", Matchers.hasProperty("password",
                        Matchers.containsString("not_important")))) // fresh new clean User for the form
                .andExpect(model().attribute("userNew", Matchers.hasProperty("activationToken",
                        Matchers.nullValue()))) // fresh new clean User for the form
                .andExpect(model().attributeDoesNotExist("addError"))
                .andExpect(model().attribute("currentUser", // logged in user with ADMIN privileges
                        Matchers.hasProperty("roles", Matchers.hasItem(Matchers.<UserRole>hasProperty(
                                "role", Matchers.containsString("ROLE_ADMIN"))))));

        // after adding, USER
        userNewUSER = userRepository.findByEmail("newUser@test.com");
        assertNotNull(userNewUSER, "userNewUSER is null");
        assertEquals("New User", userNewUSER.getName(), "userNewUSER incorrect name");
        assertEquals("newUser@test.com", userNewUSER.getEmail(), "userNewUSER incorrect email");
        assertNotEquals("not_important", userNewUSER.getPassword(), "incorrect password generated");

        // 10 digit long random pswd generated and then encoded
        assertNotEquals(10, userNewUSER.getPassword().length(),
                "incorrect generated password length, probably not encoded");
        assertNotNull(userNewUSER.getActivationToken(), "activation token null");

//        assertTrue(userNewUSER.getRoles().contains(defaultRole));  // don't know why but it returns false
        List<UserRole> roles = userNewUSER.getRoles().stream().collect(Collectors.toList());
        assertEquals(defaultRole.getRole(), roles.get(0).getRole(), " UserRole incorrect, should be USER");
    }

    @Test
    @DisplayName("AdminController - @/user/add/adminRole - creates ADMIN, by ADMIN - success")
    public void adminControllerTest_16() throws Exception {

        UserRole adminRole = roleRepository.findByRole(Role.ADMIN.getText());

        User userNewADMIN = new User();
        userNewADMIN.setPassword("not_important");

        mockMvc.perform(post("/user/add/adminRole").with(csrf()).with(latiTest()) // latiTest() = ADMIN
                .flashAttr("user", userNewADMIN)
                .param("name", "New Admin")
                .param("email", "newAdmin@test.com")
                .param("password", "not_important")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("addUser"))
                .andExpect(model().attributeDoesNotExist("nameTaken"))
                .andExpect(model().attributeDoesNotExist("emailTaken"))
         //       .andExpect(model().attribute("userNew", Matchers.hasProperty("password",
         //               Matchers.containsString("not_important")))) // fresh new clean User for the form
                .andExpect(model().attribute("userNew", Matchers.hasProperty("activationToken",
                        Matchers.nullValue()))) // fresh new clean User for the form
                .andExpect(model().attributeDoesNotExist("addError"))
                .andExpect(model().attribute("currentUser", // logged in user with ADMIN privileges
                        Matchers.hasProperty("roles", Matchers.hasItem(Matchers.<UserRole>hasProperty(
                                "role", Matchers.containsString("ROLE_ADMIN"))))));

        // after adding, ADMIN
        userNewADMIN = userRepository.findByEmail("newAdmin@test.com");
        assertNotNull(userNewADMIN, "userNewADMIN is null");
        assertEquals("New Admin", userNewADMIN.getName(), "userNewADMIN incorrect name");
        assertEquals("newAdmin@test.com", userNewADMIN.getEmail(), "userNewUSER incorrect email");
        assertNotEquals("not_important", userNewADMIN.getPassword(), "incorrect password generated");

        // 10 digit long random pswd generated and then encoded
        assertNotEquals(10, userNewADMIN.getPassword().length(),
                "incorrect generated password length, probably not encoded");
        assertNotNull(userNewADMIN.getActivationToken(), "activation token null");

//        assertTrue(userNewADMIN.getRoles().contains(adminRole));  // don't know why but it returns false
        List<UserRole> roles = userNewADMIN.getRoles().stream().collect(Collectors.toList());
        assertEquals(adminRole.getRole(), roles.get(0).getRole(), " UserRole incorrect, should be ADMIN");
    }

    @Test
    @DisplayName("AdminController - @/user/add/defaultRole - attempts to create USER, ADMIN with taken name/email - " +
            "failure")
    public void adminControllerTest_17() throws Exception {

        User userNewUSER = new User();
        userNewUSER.setPassword("not_important");

        mockMvc.perform(post("/user/add/defaultRole").with(csrf()).with(latiTest()) // latiTest() = ADMIN
                .flashAttr("user", userNewUSER)
                .param("name", "Demo") // existing name in DB
                .param("email", "newUser@test.com")
                .param("password", "not_important")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("addUser"))
                .andExpect(model().attribute("nameTaken", "Demo"))
                .andExpect(model().attributeDoesNotExist("emailTaken"))
                .andExpect(model().attribute("currentUser", // logged in user with ADMIN privileges
                        Matchers.hasProperty("roles", Matchers.hasItem(Matchers.<UserRole>hasProperty(
                                "role", Matchers.containsString("ROLE_ADMIN"))))));

        // after adding attempt, USER
        userNewUSER = userRepository.findByEmail("newUser@test.com");
        assertNull(userNewUSER, "userNewUSER is not null");

        User userNewADMIN = new User();
        userNewADMIN.setPassword("not_important");

        mockMvc.perform(post("/user/add/adminRole").with(csrf()).with(latiTest()) // latiTest() = ADMIN
                .flashAttr("user", userNewADMIN)
                .param("name", "New Admin") // existing name in DB
                .param("email", "latidude99test@gmail.com") // existing email in DB
                .param("password", "not_important")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("addUser"))
                .andExpect(model().attribute("emailTaken", "latidude99test@gmail.com"))
                .andExpect(model().attributeDoesNotExist("nameTaken"))
                .andExpect(model().attribute("currentUser", // logged in user with ADMIN privileges
                        Matchers.hasProperty("roles", Matchers.hasItem(Matchers.<UserRole>hasProperty(
                                "role", Matchers.containsString("ROLE_ADMIN"))))));

        // after adding attempt, ADMIN
        userNewADMIN = userRepository.findByName("New Admin");
        assertNull(userNewADMIN, "userNewADMIN is not null");

    }







}

//   @WithMockUser(username = "latidude99@gmail.com", password = "0011100", roles = "APPADMIN") // latiDude
//   @WithMockUser(username = "latidude99test@gmail.com", password = "1100011", roles = "ADMIN") //latiTest
//   @Sql(scripts = "/admin-controller-integration-test_2_add_user.sql", executionPhase = BEFORE_TEST_METHOD)













