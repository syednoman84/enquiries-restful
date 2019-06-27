package com.latidude99.layer;

import com.latidude99.model.Role;
import com.latidude99.model.User;
import com.latidude99.model.UserRole;
import com.latidude99.repository.UserRepository;
import com.latidude99.repository.UserRoleRepository;
import com.latidude99.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

/*
 * Uses pre-defined DB entries in data.sql
 */
@Tag("medium")
@TestPropertySource(locations = "/test.properties")
@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import({UserService.class, BCryptPasswordEncoder.class})
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
public class UserServiceLayerTests {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserRoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("UserService - encodes pswds and activates users loaded from .sql file")
    @Sql(scripts = "/user-service-layer-test_1_add_users.sql", executionPhase = BEFORE_TEST_METHOD) // 3 new users
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void userServiceTest_1(){

        userService.addDbUser(Role.APPADMIN);
        userService.addDbUser(Role.ADMIN);
        userService.addDbUser(Role.DEFAULT);

        User userUSER = userRepository.findByName("Test User USER");
        User userADMIN = userRepository.findByName("Test User ADMIN");
        User userAPPADMIN = userRepository.findByName("Test User APPADMIN");

        assertAll("USER",
                () -> assertNotNull(userUSER, "USER null"),
                () -> assertEquals("test_user_USER@test.com", userUSER.getEmail(),
                        "USER email incorrect"),
                () -> assertTrue(userUSER.isEnabled()),
                () -> assertTrue(passwordEncoder.matches("plain_text_password_USER", userUSER.getPassword()),
                        "password incorrect, probably not encoded"));

        assertAll("ADMIN",
                () -> assertNotNull(userADMIN, "ADMIN null"),
                () -> assertEquals("test_user_ADMIN@test.com", userADMIN.getEmail(),
                        "ADMIN email incorrect"),
                () -> assertTrue(userADMIN.isEnabled()),
                () -> assertTrue(passwordEncoder.matches("plain_text_password_ADMIN", userADMIN.getPassword()),
                        "ADMIN password incorrect, probably not encoded"));

        assertAll("APPADMIN",
                () -> assertNotNull(userUSER, "APPADMIN null"),
                () -> assertEquals("test_user_APPADMIN@test.com", userAPPADMIN.getEmail(),
                        "APPADMIN email incorrect"),
                () -> assertTrue(userAPPADMIN.isEnabled()),
                () -> assertTrue(passwordEncoder.matches("plain_text_password_APPADMIN", userAPPADMIN.getPassword()),
                        "APPADMIN password incorrect, probably not encoded"));

    }

    @Test
    @DisplayName("UserService - adds users with a Role, checks if name/email available")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void userServiceTest_2() {  //  XXX = USER, ADMIN or APPADMIN

        User userUSER = new User();
        userUSER.setName("User");
        userUSER.setEmail("user@user.com");
        userUSER.setPassword("1234uytre567890");

        User userADMIN = new User();
        userADMIN.setName("Admin");
        userADMIN.setEmail("Admin@admin.com");
        userADMIN.setPassword("3456jhgfd8643432");

        userService.addWithDefaultRole(userUSER);
        userService.addWithAdminRole(userADMIN);


        UserRole userRole = roleRepository.findByRole(Role.DEFAULT.getText());
        UserRole adminRole = roleRepository.findByRole(Role.ADMIN.getText());

        User userUSERAfter = userRepository.findByName("User");
        User userADMINAfter = userRepository.findByName("Admin");

        System.out.println(userUSERAfter);
        System.out.flush();


        // tests addWithXXXRole(Role role) methods
        assertAll("Roles",
                () -> assertNotNull(userUSERAfter.getRoles()),
                () -> assertNotNull(userADMINAfter.getRoles()),
                () -> assertEquals(userRole.getDescription(), userUSERAfter.getRoles().stream()
                        .collect(Collectors.toList()).get(0).getDescription(),
                        "incorrect role for USER"),
                () -> assertEquals(adminRole.getDescription(), userADMINAfter.getRoles().stream()
                        .collect(Collectors.toList()).get(0).getDescription(),
                        "incorrect role for ADMIN"));

        // tests isNameAvailable/isEmailAvailable(User user) checks
        assertAll("availability",
                () -> assertFalse(userService.isNameAvailable(userUSERAfter), "taken name available"),
                () -> assertFalse(userService.isEmailAvailable(userADMINAfter), "taken email available"));
    }

    @Test
    @DisplayName("UserService - creates a List<String> of users names and adds 'any user'")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void userServiceTest_3() {

        List<String> userNamesAsString = userService.getUserListAsStringList();

        assertAll("usersNames",
                () -> assertTrue(userNamesAsString.contains("Demo"), "no 'Demo' name on the list"),
                () -> assertTrue(userNamesAsString.contains("Lati Test"), "no 'Lati Test' name on the list"),
                () -> assertTrue(userNamesAsString.contains("Lati Dude"), "no 'Lati Dude' name on the list"),
                () -> assertTrue(userNamesAsString.contains("Test"), "no 'Test' name on the list"),
                () -> assertTrue(userNamesAsString.contains("any user"), "no 'any user' name on the list"));

    }


}
























