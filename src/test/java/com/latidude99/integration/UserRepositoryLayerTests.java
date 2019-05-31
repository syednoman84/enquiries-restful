package com.latidude99.integration;

import com.latidude99.model.Enquiry;
import com.latidude99.model.Role;
import com.latidude99.model.User;
import com.latidude99.model.UserRole;
import com.latidude99.repository.EnquiryRepository;
import com.latidude99.repository.UserRepository;
import com.latidude99.repository.UserRoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

/*
 * Uses pre-defined DB entries in data.sql
 */
@Tag("medium")
@TestPropertySource(locations = "/test.properties")
@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryLayerTests {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    UserRepository userRepository;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("UserRepository - number of users")
    public void userRepositoryTest1(){
        int expectedUserNumber = 4;

        List<User> userList = userRepository.findAll();
        int actualUserNumber = userList.size();

        assertEquals(expectedUserNumber, actualUserNumber, "incorrect number of users");
    }

    @Test
    @DisplayName("UserRepository - pre-defined user properties")
    public void userRepositoryTest2(){
        User expectedUser = new User();
        expectedUser.setName("Demo");
        expectedUser.setEmail("demo@demo.com");
        expectedUser.setPassword("111111");

        User actualUser1 = userRepository.findByEmail("demo@demo.com");
        User actualUser2 = userRepository.findByName("demo");

        assertEquals(expectedUser.getName(), actualUser1.getName(), "incorrect user name");
        assertEquals(expectedUser.getEmail(), actualUser1.getEmail(), "incorrect user email");

        assertEquals(expectedUser.getName(), actualUser2.getName(), "incorrect user name");
        assertEquals(expectedUser.getEmail(), actualUser2.getEmail(), "incorrect user email");

        assertTrue(actualUser1.isEnabled(), "incorrect value for enabled property");
        assertFalse(actualUser2.isBlocked(), "incorrect value for blocked property");

        String expectedRoleDescription = "Default role for a new user";
        String actualRoleDescription = actualUser1.getRoles().stream()
                .collect(Collectors.toList())
                .get(0)
                .getDescription();

        assertEquals(expectedRoleDescription, actualRoleDescription, "incorrect userRole");
    }



    @Test
    @DisplayName("UserRepository - save/find new users")
    public void userRepositoryTest3() {

        User user1 = new User();
        user1.setName("User Test 1");
        user1.setEmail("user1@user.com");
        user1.setEnabled(false);
        user1.setPassword(passwordEncoder.encode("passworduser1"));
        user1.setBlocked(false);
        Set<UserRole> roles1 = new HashSet<>();
        UserRole roleUser = new UserRole();
        roleUser.setRole(Role.DEFAULT.getText());
        roles1.add(roleUser);
        user1.setRoles(roles1);

        User user2 = new User();
        user2.setName("User Test 2");
        user2.setEmail("user2@user.com");
        user2.setEnabled(false);
        user2.setPassword(passwordEncoder.encode("passworduser2"));
        user2.setBlocked(true);
        Set<UserRole> roles2 = new HashSet<>();
        UserRole roleAdmin = new UserRole();
        roleAdmin.setRole(Role.ADMIN.getText());
        roleAdmin.setDescription("test user role: ADMIN");
        roles2.add(roleAdmin);
        user2.setRoles(roles2);

        testEntityManager.persist(user1);
        testEntityManager.persist(user2);
        testEntityManager.flush();

        User user1Found = userRepository.findByEmail(user1.getEmail());
        User user2Found = userRepository.findByName(user2.getName());

        String roleUser2Found = user2Found.getRoles().stream()
                .collect(Collectors.toList())
                .get(0)
                .getRole();

        assertEquals(6, userRepository.findAll().size());

        assertAll("user properties match",
                () -> assertEquals(user1Found.getName(), user1.getName()),
                () -> assertEquals(user1Found.isEnabled(), user1.isEnabled()),
                () -> assertEquals(user2Found.getEmail(), user2.getEmail()),
                () -> assertEquals("ROLE_ADMIN", roleUser2Found),
                () -> assertEquals(user2Found.isBlocked(), user2.isBlocked()),
                () -> assertTrue(passwordEncoder.matches(
                    "passworduser2", user2Found.getPassword()), "password encoder not working"));
    }

}
























