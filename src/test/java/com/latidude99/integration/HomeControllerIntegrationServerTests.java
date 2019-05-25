package com.latidude99.integration;

import com.latidude99.web.controller.HomeController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/*
 * Real server
 */

@Tag("slow")
@Tag("serverReal")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "/test.properties")
@AutoConfigureMockMvc
@AutoConfigureWebTestClient
public class HomeControllerIntegrationServerTests {

    private URL baseUrl;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private HomeController homeController;

    @BeforeEach
    public void init() throws Exception{
        this.baseUrl = new URL("http://localhost:" + port + "/");
    }

    @Test
    public void homeControllerTest_0() {

        assertThat(homeController).isNotNull();
    }

    @Test
    public void homeControllerTest_2() throws Exception {

        assertThat(this.restTemplate.getForObject(baseUrl.toString(),
                String.class)).contains("Remove Polygon");
    }



}
