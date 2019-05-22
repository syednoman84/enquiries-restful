package com.latidude99.integration;

import com.latidude99.repository.UserRepository;
import com.latidude99.service.EnquiryService;
import com.latidude99.service.UserService;
import com.latidude99.web.controller.HomeController;
import com.latidude99.web.controller.UserController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/*
 * Mock server
 * @ActiveProfiles("test") - exclude AppConfig class that makes calls to database
 * when starting the app and context wouldn't load with it
 * (secure=false) - to work around Spring Security authentication
 */

@Tag("slow")
@Tag("serverMock")
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = HomeController.class)
@TestPropertySource(locations = "/test.properties")
@AutoConfigureMockMvc(secure=false)
public class HomeControllerIntegrationTest {

    @Autowired
    private HomeController homeController;

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("HomeController - isNotNull check")
    public void homeControllerTest_0() {
        assertThat(homeController).isNotNull();
    }

    @Test
    @DisplayName("HomeController - returned enqyiryForm.html check")
    public void homeControllerTest_1() throws Exception {
        this.mvc
                .perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("enquiryForm"))
                .andExpect(content().string(containsString("Remove Polygon")))
                .andDo(MockMvcResultHandlers.print());
    }

}
