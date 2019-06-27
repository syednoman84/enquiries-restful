package com.latidude99.integration.rest;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.latidude99.web.rest.AuthenticationControllerRest;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.URL;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
public class AuthenticationControllerRestIntegrationTests_RestAssured {

    @Autowired
    private AuthenticationControllerRest authenticationControllerRest;

    @Autowired
    CsrfTokenRepository csrfTokenRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @LocalServerPort
    private int port;

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


        // using csrfTokenRepository() bean from SecurityConfig
//        CsrfToken csrfToken = csrfTokenRepository.generateToken(null);
//        Response response = given().log().all()
//                .auth()
//                .preemptive()
//                .basic("demo@demo.com", "111111")
//                .cookie("XSRF-TOKEN", csrfToken.getToken())
//                .header("X-XSRF-TOKEN",csrfToken.getToken())
//                .when()
//                .post(baseUrl.toString() + "/api/enquiry/list")
//        .then()
//                .assertThat()
//                .statusCode(HttpStatus.OK.value())
//                .extract().response();


        // without csrfTokenRepository() bean from SecurityConfig
        Response response =
                given()
                        .auth()
                        .preemptive()
                        .basic("demo@demo.com", "111111")
                .when()  // call to any restricted url
                     .get(baseUrl.toString() + "/api/enquiry/list")
                .then()
                        .log().all()
                        .extract().response();

        response = given().log().all()
                .auth()
                .preemptive()
                .basic("demo@demo.com", "111111")
                .cookie("XSRF-TOKEN", response.cookie("XSRF-TOKEN"))
                .header("X-XSRF-TOKEN", response.cookie("XSRF-TOKEN"))
        .when()
                .post(baseUrl.toString() + "/login")
         .then()
                .log().all()
                .assertThat().statusCode(200)
                .extract().response();;

        String json = response.getBody().toString();
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















