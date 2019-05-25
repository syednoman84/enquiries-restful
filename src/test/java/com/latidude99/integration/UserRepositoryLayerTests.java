package com.latidude99.integration;

import com.latidude99.model.Role;
import com.latidude99.model.User;
import com.latidude99.model.UserRole;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Tag("slow")
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
    @DisplayName("UserRepository test - save/find users")
    public void userTest1() {

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
        roleUser.setRole(Role.ADMIN.getText());
        roles2.add(roleAdmin);
        user2.setRoles(roles2);

        testEntityManager.persist(user1);
        testEntityManager.persist(user2);
        testEntityManager.flush();

        User user1Found = userRepository.findByEmail(user1.getEmail());
        User user2Found = userRepository.findByName(user2.getName());

        System.out.println(user2Found.getPassword());

        assertAll("user save/find/password match",
            () -> assertEquals(user1Found.getName(), user1.getName()),
            () -> assertEquals(user2Found.getEmail(), user2.getEmail()),
            () -> assertTrue(passwordEncoder.matches(
                    "passworduser2", user2Found.getPassword())));




    }


}
























