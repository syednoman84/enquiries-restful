package com.latidude99.integration.rest;

import com.latidude99.model.Role;
import com.latidude99.model.User;
import com.latidude99.model.UserRole;
import com.latidude99.repository.UserRepository;
import com.latidude99.repository.UserRoleRepository;
import com.latidude99.web.rest.AdminControllerRest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static com.latidude99.CustomSecurityMockMvcRequestPostProcessors.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS) // @Rollback doesn't always work
@Transactional
@Rollback
public class AdminControllerRestIntegrationTests_MockMvc {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AdminControllerRest adminControllerRest;

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
        assertThat(adminControllerRest).isNotNull();
    }

    @Test
    @DisplayName("AdminControllerRest - @/api/admin/{userId}/priviledges/add " +
            "- add ADMIN to non-existent USER /failure")
    public void adminControllerTest_1() throws Exception {

        User userToUpdateUSER = userRepository.findById(10);
        assertNull(userToUpdateUSER, "user not null before");

        mockMvc.perform(post("/api/admin/10/priviledges/add") // user.id=10 doesn't exist
                .with(csrf()).with(latiTest()) // latiTest() is ADMIN
                .param("priviledges", "ADMIN" )
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isNotFound());

        userToUpdateUSER = userRepository.findById(10);
        assertNull(userToUpdateUSER, "user not null after");
    }

    @Test
    @DisplayName("AdminControllerRest - @/api/admin/{userId}/priviledges/add " +
            "- add USER to ADMIN by USER /failure")
    public void adminControllerTest_2() throws Exception {

        UserRole roleUser = roleRepository.findByRole(Role.DEFAULT.getText());
        UserRole roleAdmin = roleRepository.findByRole(Role.ADMIN.getText());
        UserRole roleAppAdmin = roleRepository.findByRole(Role.APPADMIN.getText());

        User userToUpdateUSER = userRepository.findByEmail("test@test.com");
        assertTrue(!userToUpdateUSER.getRoles().contains(roleAdmin)
                && !userToUpdateUSER.getRoles().contains(roleAppAdmin),
                "user is ADMIN or APPADMIN before failed attempt");
        assertTrue(userToUpdateUSER.getRoles().contains(roleUser),
                "user is not USER before failed attempt");

        mockMvc.perform(post("/api/admin/3/priviledges/add") // user.id=3 is USER
                .with(csrf()).with(demo()) // demo() is USER
                .param("priviledges", "ADMIN" )
                .param("priviledges", "APPADMIN")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isForbidden());

        userToUpdateUSER = userRepository.findByEmail("test@test.com");
        assertTrue(!userToUpdateUSER.getRoles().contains(roleAdmin)
                        && !userToUpdateUSER.getRoles().contains(roleAppAdmin),
                "user is ADMIN or APPADMIN after failed attempt");
        assertTrue(userToUpdateUSER.getRoles().contains(roleUser),
                "user is not USER after failed attempt");
    }

    @Test
    @DisplayName("AdminControllerRest - @/api/admin/{userId}/priviledges/add " +
            "- add ADMIN to USER, by ADMIN /success")
//    @Transactional
//    @Rollback
    public void adminControllerTest_3() throws Exception {

        UserRole roleUser = roleRepository.findByRole(Role.DEFAULT.getText());
        UserRole roleAdmin = roleRepository.findByRole(Role.ADMIN.getText());
        UserRole roleAppAdmin = roleRepository.findByRole(Role.APPADMIN.getText());

        User userToUpdateUSER = userRepository.findByEmail("test@test.com");
        assertTrue(!userToUpdateUSER.getRoles().contains(roleAdmin)
                        && !userToUpdateUSER.getRoles().contains(roleAppAdmin),
                "user is ADMIN or APPADMIN before successful attempt");
        assertTrue(userToUpdateUSER.getRoles().contains(roleUser),
                "user is not USER before failed attempt");

        mockMvc.perform(post("/api/admin/3/priviledges/add")
                .with(csrf()).with(latiTest()) // latiTest() is ADMIN
                .param("priviledges", "ADMIN" )
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isAccepted());

        userToUpdateUSER = userRepository.findByEmail("test@test.com");
        assertTrue(userToUpdateUSER.getRoles().contains(roleAdmin)
                        && !userToUpdateUSER.getRoles().contains(roleAppAdmin)
                        && userToUpdateUSER.getRoles().contains(roleUser),
                "user is not ADMIN or is APPADMIN after successful attempt");
    }

    @Test
    @DisplayName("AdminControllerRest - @/api/admin/{userId}/priviledges/add " +
            "- add APPADMIN to ADMIN, by ADMIN (same user) /failure")
    public void adminControllerTest_4() throws Exception {

        UserRole roleAdmin = roleRepository.findByRole(Role.ADMIN.getText());
        UserRole roleAppAdmin = roleRepository.findByRole(Role.APPADMIN.getText());

        User userToUpdateADMIN = userRepository.findByEmail("latidude99test@gmail.com");
        assertTrue(userToUpdateADMIN.getRoles().contains(roleAdmin)
                        && !userToUpdateADMIN.getRoles().contains(roleAppAdmin),
                "user is not ADMIN or is APPADMIN before failed attempt");

        mockMvc.perform(post("/api/admin/2/priviledges/add") // user.Id=1 is APPADMIN
                .with(csrf()).with(latiTest()) // latiTest() is ADMIN
                .param("priviledges", "APPADMIN")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isForbidden());

        userToUpdateADMIN = userRepository.findByEmail("latidude99test@gmail.com");
        assertTrue(userToUpdateADMIN.getRoles().contains(roleAdmin)
                        && !userToUpdateADMIN.getRoles().contains(roleAppAdmin),
                "user is APPADMIN after failed attempt");
    }

    @Test
    @DisplayName("AdminControllerRest - @/api/admin/{userId}/priviledges/add " +
            "- add APPADMIN to USER, by ADMIN /failure")
    public void adminControllerTest_5() throws Exception {

        UserRole roleUser = roleRepository.findByRole(Role.DEFAULT.getText());
        UserRole roleAdmin = roleRepository.findByRole(Role.ADMIN.getText());
        UserRole roleAppAdmin = roleRepository.findByRole(Role.APPADMIN.getText());

        User userToUpdateUSER = userRepository.findByEmail("test@test.com");
        assertTrue(!userToUpdateUSER.getRoles().contains(roleAdmin)
                        && !userToUpdateUSER.getRoles().contains(roleAppAdmin),
                "user is ADMIN or APPADMIN before failed attempt");
        assertTrue(userToUpdateUSER.getRoles().contains(roleUser),
                "user is not USER before failed attempt");

        mockMvc.perform(post("/api/admin/3/priviledges/add") // user.Id=3 is USER
                .with(csrf()).with(latiTest()) // latiTest() is ADMIN
                .param("priviledges", "APPADMIN")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isForbidden());

        userToUpdateUSER = userRepository.findByEmail("test@test.com");
        assertTrue(!userToUpdateUSER.getRoles().contains(roleAdmin)
                        && !userToUpdateUSER.getRoles().contains(roleAppAdmin),
                "user is ADMIN or APPADMIN after failed attempt");
        assertTrue(userToUpdateUSER.getRoles().contains(roleUser),
                "user is not USER after failed attempt");
    }

    @Test
    @DisplayName("AdminControllerRest - @/api/admin/{userId}/priviledges/add " +
            "- add APPADMIN to ADMIN, by ADMIN (same user) /failure")
    public void adminControllerTest_6() throws Exception {

        UserRole roleAdmin = roleRepository.findByRole(Role.ADMIN.getText());
        UserRole roleAppAdmin = roleRepository.findByRole(Role.APPADMIN.getText());

        User userToUpdateADMIN = userRepository.findByEmail("latidude99test@gmail.com");
        assertTrue(userToUpdateADMIN.getRoles().contains(roleAdmin)
                        && !userToUpdateADMIN.getRoles().contains(roleAppAdmin),
                "user is not ADMIN or is APPADMIN before failed attempt");

        mockMvc.perform(post("/api/admin/1/priviledges/add") // user.Id=1 is APPADMIN
                .with(csrf()).with(latiTest()) // latiTest() is ADMIN
                .param("priviledges", "APPADMIN")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isForbidden());

        userToUpdateADMIN = userRepository.findByEmail("latidude99test@gmail.com");
        assertTrue(userToUpdateADMIN.getRoles().contains(roleAdmin)
                        && !userToUpdateADMIN.getRoles().contains(roleAppAdmin),
                "user is APPADMIN after failed attempt");
    }

    @Test
    @DisplayName("AdminControllerRest - @/api/admin/{userId}/priviledges/remove " +
            "- remove ADMIN from ADMIN by USER /failure")
    public void adminControllerTest_7() throws Exception {

        UserRole roleAdmin = roleRepository.findByRole(Role.ADMIN.getText());
        User userToUpdateADMIN = userRepository.findByEmail("latidude99test@gmail.com");

        assertTrue(userToUpdateADMIN.getRoles().contains(roleAdmin),
                "user is not ADMIN before failed attempt");

        mockMvc.perform(post("/api/admin/3/priviledges/remove") // user.id=3 is USER
                .with(csrf()).with(demo()) // demo() is USER
                .param("priviledges", "ADMIN" )
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isForbidden());

        userToUpdateADMIN = userRepository.findByEmail("latidude99test@gmail.com");

        assertTrue(userToUpdateADMIN.getRoles().contains(roleAdmin),
                "user is not ADMIN after failed attempt");
    }

    @Test
    @DisplayName("AdminControllerRest - @/api/admin/{userId}/priviledges/remove " +
            "- remove USER from USER, by ADMIN /success")
    public void adminControllerTest_8() throws Exception {

        UserRole roleUser = roleRepository.findByRole(Role.DEFAULT.getText());
        UserRole roleAdmin = roleRepository.findByRole(Role.ADMIN.getText());
        UserRole roleAppAdmin = roleRepository.findByRole(Role.APPADMIN.getText());

        User userToUpdateUSER = userRepository.findByEmail("test@test.com");
        assertTrue(!userToUpdateUSER.getRoles().contains(roleAdmin)
                        && !userToUpdateUSER.getRoles().contains(roleAppAdmin),
                "user is ADMIN or APPADMIN before successful attempt");
        assertTrue(userToUpdateUSER.getRoles().contains(roleUser),
                "user is not USER before successful attempt");

        mockMvc.perform(post("/api/admin/3/priviledges/remove") // userId=3 is USER
                .with(csrf()).with(latiTest()) // latiTest() is ADMIN
                .param("priviledges", "USER" )
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isAccepted());

        userToUpdateUSER = userRepository.findByEmail("test@test.com");
        assertTrue(!userToUpdateUSER.getRoles().contains(roleAdmin)
                        && !userToUpdateUSER.getRoles().contains(roleAppAdmin),
                "user is ADMIN or APPADMIN after successful attempt");
        assertTrue(!userToUpdateUSER.getRoles().contains(roleUser),
                "user is USER after successful attempt");
    }

    @Test
    @DisplayName("AdminControllerRest - @/api/admin/{userId}/priviledges/remove " +
            "- remove ADMIN from ADMIN, by ADMIN (same user) /failure")
    public void adminControllerTest_9() throws Exception {

        UserRole roleAdmin = roleRepository.findByRole(Role.ADMIN.getText());
        UserRole roleAppAdmin = roleRepository.findByRole(Role.APPADMIN.getText());

        User userToUpdateADMIN = userRepository.findByEmail("latidude99test@gmail.com");
        assertTrue(userToUpdateADMIN.getRoles().contains(roleAdmin)
                        && !userToUpdateADMIN.getRoles().contains(roleAppAdmin),
                "user is not ADMIN or is APPADMIN before failed attempt");

        mockMvc.perform(post("/api/admin/2/priviledges/remove") // userId=2 is ADMIN
                .with(csrf()).with(latiTest()) // latiTest() is ADMIN
                .param("priviledges", "ADMIN")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isForbidden());

        userToUpdateADMIN = userRepository.findByEmail("latidude99test@gmail.com");
        assertTrue(userToUpdateADMIN.getRoles().contains(roleAdmin)
                        && !userToUpdateADMIN.getRoles().contains(roleAppAdmin),
                "user is  APPADMIN after failed attempt");
    }

    @Test
    @DisplayName("AdminControllerRest - @/api/admin/{userId}/priviledges/remove " +
            "- remove ADMIN from ADMIN, by APPADMIN /success")
    public void adminControllerTest_10() throws Exception {

        UserRole roleAdmin = roleRepository.findByRole(Role.ADMIN.getText());
        UserRole roleAppAdmin = roleRepository.findByRole(Role.APPADMIN.getText());

        User userToUpdateADMIN = userRepository.findByEmail("latidude99test@gmail.com");
        assertTrue(userToUpdateADMIN.getRoles().contains(roleAdmin)
                        && !userToUpdateADMIN.getRoles().contains(roleAppAdmin),
                "user is not ADMIN or is APPADMIN before successful attempt");

        mockMvc.perform(post("/api/admin/2/priviledges/remove") // userId=2 is ADMIN
                .with(csrf()).with(latiDude()) // latiDude() is APPADMIN
                .param("priviledges", "ADMIN")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isAccepted());

        userToUpdateADMIN = userRepository.findByEmail("latidude99test@gmail.com");
        assertTrue(!userToUpdateADMIN.getRoles().contains(roleAdmin)
                        && !userToUpdateADMIN.getRoles().contains(roleAppAdmin),
                "user is ADMIN after successful attempt");
    }

    @Test
    @DisplayName("AdminControllerRest - @/api/admin/{userId}/block " +
            "- block/unblock users as ADMIN")
    public void adminControllerTest_11() throws Exception {

        User userToUpdateUSER = userRepository.findByEmail("demo@demo.com");
        assertFalse(userToUpdateUSER.isBlocked(),
                "test user blocked before blocking"); // USER not blocked

        mockMvc.perform(post("/api/admin/4/block") //user.Id=4 is USER
                .with(csrf()).with(latiTest()) // latiTest is ADMIN
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isAccepted());

        // after blocking
        assertTrue(userRepository.findByEmail("demo@demo.com").isBlocked(),
                "test USER not blocked after blocking");

        mockMvc.perform(post("/api/admin/4/block") //user.Id=4 is USER
                .with(csrf()).with(latiTest()) // latiTest is ADMIN
                .flashAttr("user", userToUpdateUSER)
                .param("id", "" + userToUpdateUSER.getId())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isAccepted());

        // after unblocking
        assertFalse(userRepository.findByEmail("demo@demo.com").isBlocked(),
                "test USER not enabled after unblocking");
    }

    @Test
    @DisplayName("AdminControllerRest - @/api/admin/{userId}/block" +
            " - block/unblock users as USER")
    public void adminControllerTest_12() throws Exception {

        User userToUpdateADMIN = userRepository.findByEmail("latidude99test@gmail.com");
        assertFalse(userToUpdateADMIN.isBlocked(),
                "test ADMIN blocked before blocking");

        mockMvc.perform(post("/api/admin/2/block") //user.Id=2 is ADMIN
                .with(csrf()).with(demo()) // demo() = USER
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isForbidden());

        // after blocking attempt
        userToUpdateADMIN = userRepository.findByEmail("latidude99test@gmail.com");
        assertFalse(userToUpdateADMIN.isBlocked(),
                "test ADMIN blocked after blocking");
    }



    @Test
    @DisplayName("AdminControllerRest - @/api/admin/{userId}/disable " +
            "- enable/disable users as ADMIN")
    public void adminControllerTest_13() throws Exception {

        User userToUpdateUSER = userRepository.findByEmail("demo@demo.com");
        assertTrue(userToUpdateUSER.isEnabled(),
                "test USER disabled before disabling");

        mockMvc.perform(post("/api/admin/4/disable") // user.Id=4 is USER
                .with(csrf()).with(latiTest()) // latiTest is ADMIN
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isAccepted());

        // after disabling
        userToUpdateUSER = userRepository.findByEmail("demo@demo.com");
        assertFalse(userToUpdateUSER.isEnabled(),
                "test USER not disabled after disabling");

        mockMvc.perform(post("/api/admin/4/disable") // user.Id=4 is USER
                .with(csrf()).with(latiTest()) // latiTest is ADMIN
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isAccepted());

        // after enabling again
        userToUpdateUSER = userRepository.findByEmail("demo@demo.com");
        assertTrue(userToUpdateUSER.isEnabled(),
                "test USER not enabled after enabling again");
    }

    @Test
    @DisplayName("AdminControllerRest - @/api/admin/{userId}/disable " +
            "- enable/disable users as USER")
    public void adminControllerTest_14() throws Exception {

        // USER
        User userToUpdateUSER = userRepository.findByEmail("test@test.com");
        assertTrue(userToUpdateUSER.isEnabled(),
                "test USER disabled before disabling");

        mockMvc.perform(post("/api/admin/3/disable") // user.ID=3 is USER
                .with(csrf()).with(demo()) // demo is USER
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isForbidden());

        // after disabling attempt
        userToUpdateUSER = userRepository.findByEmail("test@test.com");
        assertTrue(userToUpdateUSER.isEnabled(),
                "test USER disabled after disabling");

        // ADMIN
        User userToUpdateADMIN = userRepository.findByEmail("latidude99test@gmail.com");
        assertTrue(userToUpdateADMIN.isEnabled(),
                "test ADMIN disabled before disabling");

        mockMvc.perform(post("/api/admin/2/disable") // user.ID=2 is ADMIN
                .with(csrf()).with(demo()) // demo is USER
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isForbidden());

        // after disabling attempt
        userToUpdateADMIN = userRepository.findByEmail("latidude99test@gmail.com");
        assertTrue(userToUpdateADMIN.isEnabled(),
                "test ADMIN disabled after disabling");
    }

    @Test
    @DisplayName("AdminControllerRest - @/api/admin/{userId}/reset " +
            "- reset User pswd and sends pswd reset link, by USER /forbidden")
    public void adminControllerTest_15() throws Exception {

        User userToResetUSER = userRepository.findByEmail("test@test.com");
        assertNull(userToResetUSER.getResetToken(), "reset token not null");

        mockMvc.perform(post("/api/admin/3/reset") //user.Id=3 is USER
            .with(csrf()).with(demo()) // demo is USER
            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andDo(print())
            .andExpect(status().isForbidden());

        userToResetUSER = userRepository.findByEmail("test@test.com");
        assertNull(userToResetUSER.getResetToken(), "reset token not null");
    }

    @Test
    @DisplayName("AdminControllerRest - @/api/admin/{userId}/reset " +
            "- reset User pswd and sends pswd reset link, by ADMIN")
    public void adminControllerTest_16() throws Exception {

        UserRole roleUser = roleRepository.findByRole(Role.DEFAULT.getText());
        UserRole roleAdmin = roleRepository.findByRole(Role.ADMIN.getText());
        UserRole roleAppAdmin = roleRepository.findByRole(Role.APPADMIN.getText());

        User userToResetUSER = userRepository.findByEmail("test@test.com");
        assertTrue(!userToResetUSER.getRoles().contains(roleAdmin)
                        && !userToResetUSER.getRoles().contains(roleAppAdmin),
                "user is ADMIN or APPADMIN before reset attempt");
        assertTrue(userToResetUSER.getRoles().contains(roleUser),
                "user is not USER before reset attempt");

        MvcResult mvcResult =
                 mockMvc.perform(post("/api/admin/3/reset") //user.Id=3 is USER
                .with(csrf()).with(latiTest()) // latiTest is ADMIN
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isAccepted())
                .andReturn();

        assertEquals(mvcResult.getResponse().getContentAsString(),
                "email with password reset link sent to: " +  userToResetUSER.getEmail(),
                 "incorrect responseEntity body after success");

        // after reset, USER
        userToResetUSER = userRepository.findByEmail("test@test.com");
        assertNotNull(userToResetUSER.getResetToken(), "reset token null");
    }

    @Test
    @DisplayName("AdminControllerRest - @/api/admin/{userId}/reset " +
            "- reset APPADMIN pswd and sends pswd reset link, by ADMIN /forbidden ")
    public void adminControllerTest_17() throws Exception {

        UserRole roleAppAdmin = roleRepository.findByRole(Role.APPADMIN.getText());

        User userToResetAPPADMIN = userRepository.findByEmail("latidude99@gmail.com");
        assertTrue(userToResetAPPADMIN.getRoles().contains(roleAppAdmin),
                "user is not APPADMIN before reset attempt");

        mockMvc.perform(post("/api/admin/1/reset") //user.Id=1 is APPADMIN
                 .with(csrf()).with(latiTest()) // latiTest is ADMIN
                 .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                 .andDo(print())
                 .andExpect(status().isForbidden())
                 .andExpect(jsonPath("$.errors.[0]",
                         Matchers.is("Insufficient rights for the operation")))
                .andExpect(jsonPath("$.message",
                        Matchers.containsString("Your priviledges: ")))
                 .andExpect(jsonPath("$.message",
                         Matchers.containsString(" do not allow for this operation")));

        // after reset, ADMIN
        userToResetAPPADMIN = userRepository.findByEmail("latidude99@gmail.com");
        assertNull(userToResetAPPADMIN.getResetToken(), "reset token not null");
    }

    @Test
    @DisplayName("AdminControllerRest - @/api/admin/{userId}/activate " +
            "- sends activation link to USER, by USER /forbidden")
    public void adminControllerTest_18() throws Exception {

        User  userToActivateUSER = userRepository.findByEmail("test@test.com");
        assertNull(userToActivateUSER.getActivationToken(),
                "activation token not null before activation link sending attempt");

        mockMvc.perform(post("/api/admin/3/activate") //user.Id=3 is USER
                .with(csrf()).with(demo()) // demo is USER
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isForbidden());

        userToActivateUSER = userRepository.findByEmail("test@test.com");
        assertNull(userToActivateUSER.getActivationToken(),
                "activation token not null after activation link sending attempt");
    }


    @Test
    @DisplayName("AdminControllerRest - @/api/admin/{userId}/activate " +
            "- sends activation link to USER, by ADMIN")
    public void adminControllerTest_19() throws Exception {

        UserRole roleUser = roleRepository.findByRole(Role.DEFAULT.getText());
        UserRole roleAdmin = roleRepository.findByRole(Role.ADMIN.getText());
        UserRole roleAppAdmin = roleRepository.findByRole(Role.APPADMIN.getText());

        User userToActivateUSER = userRepository.findByEmail("demo@demo.com");
        assertTrue(!userToActivateUSER.getRoles().contains(roleAdmin)
                        && !userToActivateUSER.getRoles().contains(roleAppAdmin),
                "user is ADMIN or APPADMIN before activation link sent");
        assertTrue(userToActivateUSER.getRoles().contains(roleUser),
                "user is not USER before activation link sent");
        assertNull(userToActivateUSER.getActivationToken(),
                "activation token not null before activation link sent");

        MvcResult mvcResult=
                mockMvc.perform(post("/api/admin/4/activate") // user.Id=4 is USER
                .with(csrf()).with(latiTest()) // latiTest is ADMIN
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isAccepted())
                .andReturn();

        assertEquals(mvcResult.getResponse().getContentAsString(),
                "email with activation link sent to: " +  userToActivateUSER.getEmail(),
                "incorrect response body after activation link sent");

        // after activation link sent, USER
        userToActivateUSER = userRepository.findByEmail("demo@demo.com");
        assertNotNull(userToActivateUSER.getActivationToken(),
                "activation token null after activation link sent");
    }

    @Test
    @DisplayName("AdminControllerRest - @/api/admin/{userId}/activate " +
            "- sends activation link to APPADMIN, by ADMIN /forbidden")
    public void adminControllerTest_20() throws Exception {

        UserRole roleAdmin = roleRepository.findByRole(Role.ADMIN.getText());
        UserRole roleAppAdmin = roleRepository.findByRole(Role.APPADMIN.getText());

        User userToActivateAPPADMIN = userRepository.findByEmail("latidude99@gmail.com");
        assertTrue(userToActivateAPPADMIN.getRoles().contains(roleAppAdmin),
                "user is not APPADMIN before activation link sending attempt");
        assertNull(userToActivateAPPADMIN.getActivationToken(),
                "activation token not null before activation link sending attempt");

        mockMvc.perform(post("/api/admin/1/activate") // user.Id=1 is APPADMIN
            .with(csrf()).with(latiTest()) // latiTest is ADMIN
            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andDo(print())
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.errors.[0]",
                   Matchers.is("Insufficient rights for the operation")))
            .andExpect(jsonPath("$.message",
                   Matchers.containsString("Your priviledges: ")))
            .andExpect(jsonPath("$.message",
                   Matchers.containsString(" do not allow for this operation")));

        // after activation link sending attempt, APPADMIN
        userToActivateAPPADMIN = userRepository.findByEmail("latidude99@gmail.com");
        assertNull(userToActivateAPPADMIN.getActivationToken(),
                "activation token not null after activation link sending attempt");
    }

    @Test
    @DisplayName("AdminControllerRest - @/api/admin/adduser" +
            " - creates USER, by ADMIN /success")
    @Rollback
    public void adminControllerTest_21() throws Exception {

        UserRole defaultRole = roleRepository.findByRole(Role.DEFAULT.getText());

        MvcResult mvcResult =
            mockMvc.perform(post("/api/admin/adduser")
                    .with(csrf()).with(latiTest()) // latiTest is ADMIN
                    .param("name", "New User")
                    .param("email", "newUser@test.com")
                    .param("role", "user")
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andReturn();

        assertEquals(mvcResult.getResponse().getContentAsString(),
                "new user created, an email with activation link sent to: newUser@test.com",
                "incorrect response body after adding user");

        // after adding, USER
        User userNewUSER = userRepository.findByEmail("newUser@test.com");
        assertNotNull(userNewUSER, "userNewUSER is null");
        assertEquals("New User", userNewUSER.getName(),
                "userNewUSER incorrect name");
        assertEquals("newUser@test.com", userNewUSER.getEmail(),
                "userNewUSER incorrect email");
        assertNotEquals("not_important", userNewUSER.getPassword(),
                "incorrect password generated");

        // 10 digit long random pswd generated and then encoded (BCrypt)
        // hence password length can't be 10
        assertNotEquals(10, userNewUSER.getPassword().length(),
                "incorrect generated password length, probably not encoded");
        assertNotNull(userNewUSER.getActivationToken(),
                "activation token null");

        assertTrue(userNewUSER.getRoles().contains(defaultRole),
                "UserRole incorrect, should be USER");
    }

    @Test
    @DisplayName("AdminControllerRest - @/api/admin/adduser" +
            " - creates ADMIN, by ADMIN /success")
    public void adminControllerTest_22() throws Exception {

        UserRole adminRole = roleRepository.findByRole(Role.ADMIN.getText());

        MvcResult mvcResult =
                mockMvc.perform(post("/api/admin/adduser")
                        .with(csrf()).with(latiTest()) // latiTest is ADMIN
                        .param("name", "New Admin No Conflict")
                        .param("email", "newAdmin@test.com")
                        .param("role", "admin")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                        .andDo(print())
                        .andExpect(status().isCreated())
                        .andReturn();

        assertEquals(mvcResult.getResponse().getContentAsString(),
                "new user created, an email with activation link sent to: newAdmin@test.com",
                "incorrect response body after adding user");

        // after adding, ADMIN
        User userNewADMIN = userRepository.findByEmail("newAdmin@test.com");
        assertNotNull(userNewADMIN, "userNewADMIN is null");
        assertEquals("New Admin No Conflict", userNewADMIN.getName(),
                "userNewADMIN incorrect name");
        assertEquals("newAdmin@test.com", userNewADMIN.getEmail(),
                "userNewUSER incorrect email");
        assertNotEquals("not_important", userNewADMIN.getPassword(),
                "incorrect password generated");

        // 10 digit long random pswd generated and then encoded
        assertNotEquals(10, userNewADMIN.getPassword().length(),
                "incorrect generated password length, probably not encoded");
        assertNotNull(userNewADMIN.getActivationToken(), "activation token null");

        assertTrue(userNewADMIN.getRoles().contains(adminRole),
                "UserRole incorrect, should be ADMIN");
    }

    @Test
    @DisplayName("AdminControllerRest - @/api/admin/adduser" +
            "- attempts to create ADMIN with taken email /failure")
    public void adminControllerTest_23() throws Exception {

        User userNewADMIN = userRepository.findByName("New Admin");
        assertNull(userNewADMIN, "userNewADMIN is not null before");

        mockMvc.perform(post("/api/admin/adduser")
                .with(csrf()).with(latiTest()) // latiTest is ADMIN
                .param("name", "New Admin")
                .param("email", "latidude99test@gmail.com") // existing email in DB
                .param("role", "admin")
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors.[0]",
                        Matchers.is("duplicate resource")))
                .andExpect(jsonPath("$.message",
                        Matchers.is("user with email: " +
                                "latidude99test@gmail.com" +
                                " has already been registered")));

        userNewADMIN = userRepository.findByName("New Admin");
        assertNull(userNewADMIN, "userNewADMIN is not null after");
    }

}













