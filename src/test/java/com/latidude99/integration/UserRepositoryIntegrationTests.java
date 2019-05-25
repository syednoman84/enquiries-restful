package com.latidude99.integration;

import com.latidude99.model.User;
import com.latidude99.repository.UserRepository;
import com.latidude99.repository.UserRoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Tag("slow")
@TestPropertySource(locations = "/test.properties")
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserRepositoryIntegrationTests {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserRoleRepository userRoleRepository;

    @Test
    @DisplayName("UserRepository test - number of users")
    public void userTest1(){
        int expectedUserNumber = 4;

        List<User> userList = userRepository.findAll();
        int actualUserNumber = userList.size();

        assertEquals(expectedUserNumber, actualUserNumber, "incorrect number of users");
    }

    @Test
    @DisplayName("UserRepository test - user properties")
    public void userTest2(){
        User expectedUser = new User();
        expectedUser.setName("Demo");
        expectedUser.setEmail("demo@demo.com");
        expectedUser.setPassword("111111");

        User actualUser1 = userRepository.findByEmail("demo@demo.com");
        User actualUser2 = userRepository.findByName("demo");

        assertEquals(expectedUser.getName(), actualUser1.getName(), "incorrect user name");
        assertEquals(expectedUser.getEmail(), actualUser1.getEmail(), "incorrect user email");
        assertNotEquals(expectedUser.getPassword(), actualUser1.getPassword(),
                "password encoding not working");

        assertEquals(expectedUser.getName(), actualUser2.getName(), "incorrect user name");
        assertEquals(expectedUser.getEmail(), actualUser2.getEmail(), "incorrect user email");
        assertNotEquals(expectedUser.getPassword(), actualUser2.getPassword(),
                "password encoding not working");
        assertTrue(actualUser1.isEnabled(), "incorrect value for enabled property");
        assertFalse(actualUser2.isBlocked(), "incorrect value for blocked property");

        String expectedRoleDescription = "Default role for a new user";
        String actualRoleDescription = actualUser1.getRoles().stream()
                .collect(Collectors.toList())
                .get(0)
                .getDescription();

        assertEquals(expectedRoleDescription, actualRoleDescription, "incorrect userRole");

    }

}


