package com.latidude99.integration.rest;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.latidude99.web.rest.AuthenticationControllerRest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/*
 *  - Mock server
 *  - @ActiveProfiles("test") - exclude AppConfig class that makes calls to
 *    database when starting the app and context wouldn't load with it
 *  - (secure=false) - to work around Spring Security authentication
 */

@Tag("slow")
//@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "/test.properties")
public class AuthenticationControllerRestIntegrationTests_RestTemplate {

    @Autowired
    private AuthenticationControllerRest authenticationControllerRest;

    @Autowired
    CsrfTokenRepository csrfTokenRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate template;


    @Test
    @DisplayName("AuthenticationControllerRest - isNotNull check")
    public void authenticationControllerRestTest_0() {
        assertThat(authenticationControllerRest).isNotNull();
    }


    /*
     * AuthenticationControllerRest returns Principal object
     * with the logged in user details in its properties
     */
    @Test
    @DisplayName("AuthenticationControllerRest - @/login -checks logged in user properties")
    public void userControllerTest_1() throws Exception {

        URL baseUrl = new URL("http://localhost:" + port);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        CsrfToken csrfToken = csrfTokenRepository.generateToken(null);
        headers.add(csrfToken.getHeaderName(), csrfToken.getToken());
        headers.add("Cookie", "XSRF-TOKEN=" + csrfToken.getToken());

        // old way
//        String plainCredits = "demo@demo.com:111111";
//        byte[] plainCreditsBytes = plainCredits.getBytes();
//        byte[] base64CreditsBytes = Base64.encodeBase64(plainCreditsBytes);
//        String base64Credits = new String(base64CreditsBytes);
//        headers.add("Authorization", "Basic " + base64Credits);

        // as of Spring 5.1
//      String username = "demo@demo.com";
//      String password = ":111111";
//      headers.setBasicAuth(username, password);

        TestRestTemplate template = new TestRestTemplate("demo@demo.com", "111111");

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(baseUrl.toString())
                .path("/login");

        HttpEntity<String> entityPost = new HttpEntity<>(headers);

        // call
        ResponseEntity<String> response = template
                .postForEntity(builder.toUriString(), entityPost, String.class);

        String json = response.getBody();
        DocumentContext jsonContext = JsonPath.parse(json);

        // assertions, response
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "incorrect http status code after successfull login");

        assertEquals(jsonContext.read("$.authorities.[0].authority"), "ROLE_USER",
                "incorrect logged in user role");
        assertEquals(jsonContext.read("$.authenticated"), true);
        // TestRestTemplate sends null in place of password in response (as oppose to MockMvc)
        assertNull(jsonContext.read("$.principal.password"));
        assertEquals(jsonContext.read("$.principal.username"), "demo@demo.com");
        assertEquals(jsonContext.read(
                        "$.principal.authorities.[0].authority"), "ROLE_USER");
        assertEquals(jsonContext.read("$.principal.enabled"), true);
        assertEquals(jsonContext.read("$.name"), "demo@demo.com");
                ;
    }

}















