package com.latidude99.integration.rest;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.latidude99.model.User;
import com.latidude99.repository.UserRepository;
import com.latidude99.web.rest.UserControllerRest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

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
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "/test.properties")
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
public class UserControllerRestIntegrationTests_RestTemplate {

    private HttpHeaders headers;
    private URL baseUrl;

    @Autowired
    private UserControllerRest userControllerRest;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    CsrfTokenRepository csrfTokenRepository;

    @BeforeAll
    public static void initAll(){
    }

    @AfterAll
    public static void tearDownAll(){
    }

    @BeforeEach
    public void init() throws Exception {
        this.baseUrl = new URL("http://localhost:" + port);

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        CsrfToken csrfToken = csrfTokenRepository.generateToken(null);
        headers.add(csrfToken.getHeaderName(), csrfToken.getToken());
        headers.add("Cookie", "XSRF-TOKEN=" + csrfToken.getToken());
    }

    @Test
    @DisplayName("UserController - isNotNull check")
    public void userControllerTest_0() {
        assertThat(userControllerRest).isNotNull();
    }


    @Test
    @DisplayName("UserController - @/api/user/update - logged in user password change")
//    @WithMockUser(username = "demo@demo.com", password = "111111", roles = "USER")
    public void userControllerTest_2_template() throws Exception {

        // checks before
        User user = userRepository.findByEmail("demo@demo.com");
        assertTrue(passwordEncoder.matches("111111", user.getPassword()),
                "incorrect user password before change");

        // setup
        URI uri = UriComponentsBuilder
                .fromHttpUrl(baseUrl.toString())
                .path("/api/user/update")
                .build().toUri();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("passwordOld", "111111");
        params.add("passwordNew", "222222");
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        // call
        ResponseEntity<String> response = template
                .withBasicAuth("demo@demo.com", "111111")
                .postForEntity(uri, entity, String.class);

        // assertions
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        user = userRepository.findByEmail("demo@demo.com");
        assertTrue(passwordEncoder.matches("222222", user.getPassword()),
                "incorrect user password after change");
    }


    @Test
    @DisplayName("UserController - @/api/user/activate - user activation request with token")
    @Sql(scripts = "/user-controller-integration-test_3_add_activation_token.sql", executionPhase = BEFORE_TEST_METHOD)
    public void userControllerTest_3() throws Exception {

        String token = "testActivationTokenString";
        User testUser = userRepository.findByActivationToken(token);

        //  -- failure --
        // checks before
        assertFalse(testUser.isEnabled(), "user is activated before failed attempt");

        // setup
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(baseUrl.toString())
                .path("/api/user/activate")
                .queryParam("activationToken", "invalid activation token");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // call
        ResponseEntity<String> response = template
                .exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        String json = response.getBody();
        DocumentContext jsonContext = JsonPath.parse(json);

        // assertions
        assertEquals(HttpStatus.EXPECTATION_FAILED.getReasonPhrase().toUpperCase()
                        .replace(' ', '_'),
                jsonContext.read("$.status"),
                "incorrect status after failed attempt");
        assertEquals("invalid activation token", jsonContext.read("$.message"),
                "incorrect message after failed attempt");
        assertEquals("not authorized", jsonContext.read("$.errors[0]"),
                "incorrect error");

        testUser = userRepository.findByEmail(testUser.getEmail());
        assertFalse(testUser.isEnabled(), "user is activated after failed attempt");
        assertEquals(token, testUser.getActivationToken(),
                "incorrect token value after failed activation attempt");


        //  -- success --
        // checks before
        assertFalse(testUser.isEnabled(), "user is activated before successful attempt");

        // setup
        builder = UriComponentsBuilder
                .fromHttpUrl(baseUrl.toString())
                .path("/api/user/activate")
                .queryParam("activationToken", token);
        entity = new HttpEntity<>(headers);

        // call
        response = template
                .exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        json = response.getBody();
        jsonContext = JsonPath.parse(json);

        // assertions
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode(),
                "incorrect http status code after successfull attempt");
        assertEquals("user " + testUser.getEmail() + " activated", response.getBody(),
                "incorrect reponse body after successfull attempt");

        testUser = userRepository.findByEmail(testUser.getEmail());
        assertTrue(testUser.isEnabled(), "user is not activated after successful attempt");
        assertNull(testUser.getActivationToken(),
                "token not null  value after successful attempt");

    }



    @Test
    @DisplayName("UserController - @/api/user/reset - user password reset request with token")
    @Sql(scripts = "/user-controller-integration-test_4_add_reset_token.sql", executionPhase = BEFORE_TEST_METHOD)
    public void userControllerTest_4() throws Exception {

        String token = "testResetTokenString";
        User testUser = userRepository.findByResetToken(token);

        //  -- failure --
        // checks before
        assertTrue(testUser.isEnabled(), "user not enabled before failed reset call");

        // setup
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(baseUrl.toString())
                .path("/api/user/reset")
                .queryParam("resetToken", "incorrectTestResetTokenString");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // call
        ResponseEntity<String> response = template
                .exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        String json = response.getBody();
        DocumentContext jsonContext = JsonPath.parse(json);

        // assertions
        assertEquals(HttpStatus.EXPECTATION_FAILED.getReasonPhrase().toUpperCase()
                        .replace(' ', '_'),
                jsonContext.read("$.status"),
                "incorrect status after failed attempt");
        assertEquals("invalid reset token", jsonContext.read("$.message"),
                "incorrect message after failed attempt");
        assertEquals("not authorized", jsonContext.read("$.errors[0]"),
                "incorrect error");

        testUser = userRepository.findByEmail(testUser.getEmail());
        assertEquals(token, testUser.getResetToken(),
                "incorrect token value after failed reset call");


        //  -- success --
        // checks before
        assertTrue(testUser.isEnabled(), "user not enabled before successful reset call");

        // setup
        builder = UriComponentsBuilder
                .fromHttpUrl(baseUrl.toString())
                .path("/api/user/reset")
                .queryParam("resetToken", token);
        entity = new HttpEntity<>(headers);

        // call
        response = template
                .exchange(builder.toUriString(), HttpMethod.GET, entity, String.class);

        // assertions
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode(),
                "incorrect http status code after successful attempt");
        assertEquals(token, response.getBody());

        testUser = userRepository.findByEmail(testUser.getEmail());
        assertEquals(token, testUser.getResetToken(),
                "incorrect token value after successful reset call");
    }

    @Test
    @DisplayName("UserController - @/api/user/reset - user password reset/new")
    @Sql(scripts = "/user-controller-integration-test_5_add_user.sql", executionPhase = BEFORE_TEST_METHOD)
    public void userControllerTest_5() throws Exception {

        String token = "testResetTokenString";
        User testUser = userRepository.findByResetToken(token);
        String email = testUser.getEmail();

        //  -- failure --
        // checks before (PasswordEncoder not used when @Sql script used)
        assertTrue(testUser.isEnabled(), "user not enabled before failed reset call");

        // setup
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(baseUrl.toString())
                .path("/api/user/reset");
        MultiValueMap<String, String> paramsFail = new LinkedMultiValueMap<>();
        paramsFail.add("token", "incorrectTestResetTokenString");
        paramsFail.add("password", "new reset password");
        HttpEntity<MultiValueMap<String, String>> entityPostFail = new HttpEntity<>(paramsFail, headers);

        // call
        ResponseEntity<String> response = template
                .postForEntity(builder.toUriString(), entityPostFail, String.class);

        String json = response.getBody();
        DocumentContext jsonContext = JsonPath.parse(json);

        // assertions
        assertEquals(HttpStatus.EXPECTATION_FAILED.getReasonPhrase().toUpperCase()
                        .replace(' ', '_'),
                jsonContext.read("$.status"),
                "incorrect status after failed attempt");
        assertEquals("invalid reset token", jsonContext.read("$.message"),
                "incorrect message after failed attempt");
        assertEquals("not authorized", jsonContext.read("$.errors[0]"),
                "incorrect error");

        testUser = userRepository.findByEmail(email);
        assertEquals("old_password_reset", testUser.getPassword(),
                "incorrect password after failed change attempt");



        //  -- success --
        // checks before (PasswordEncoder not used when @Sql script used)
        assertTrue(testUser.isEnabled(), "user not enabled before successful reset call");

        // setup
        builder = UriComponentsBuilder
                .fromHttpUrl(baseUrl.toString())
                .path("/api/user/reset");
        MultiValueMap<String, String> paramsSuccess = new LinkedMultiValueMap<>();
        paramsSuccess.add("token", token);
        paramsSuccess.add("password", "new reset password");
        HttpEntity<MultiValueMap<String, String>> entityPostparamsSuccess = new HttpEntity<>(paramsSuccess, headers);

        // call
        response = template
                .postForEntity(builder.toUriString(), entityPostparamsSuccess, String.class);

        json = response.getBody();
        jsonContext = JsonPath.parse(json);

        // assertions, response
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "incorrect http status code after successful reset call");
        assertNull(jsonContext.read("$.resetToken"),
                "resetToken not null after succesfull reset call");
        assertTrue(passwordEncoder.matches("new reset password",
                jsonContext.read("$.password").toString()),
                "incorrect password after successful change call, response");

        // assertions, repository
        testUser = userRepository.findByEmail(email);
        assertTrue(passwordEncoder.matches("new reset password", testUser.getPassword()),
                "incorrect password after successful change call, repository");

    }

    @Test
    @DisplayName("UserController - @/api/user/forgot - user password forgot form")
    @Sql(scripts = "/user-controller-integration-test_6_add_user.sql", executionPhase = BEFORE_TEST_METHOD)
    public void userControllerTest_6() throws Exception {
        String incorrectEmail = "wrong_user_forgot@test.com";
        User testUser = userRepository.findByEmail("test_user_forgot@test.com");

        //  -- failure --
        // checks before
        assertNotNull(testUser, "test user null before failed call");

        // setup
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(baseUrl.toString())
                .path("/api/user/forgot");
        MultiValueMap<String, String> paramsFail = new LinkedMultiValueMap<>();
        paramsFail.add("email", incorrectEmail);
        HttpEntity<MultiValueMap<String, String>> entityPostFail = new HttpEntity<>(paramsFail, headers);

        // call
        ResponseEntity<String> response = template
                .postForEntity(builder.toUriString(), entityPostFail, String.class);

        String json = response.getBody();
        DocumentContext jsonContext = JsonPath.parse(json);

        // assertions
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase().toUpperCase()
                        .replace(' ', '_'),
                jsonContext.read("$.status"),
                "no register user with the email address: " + incorrectEmail);
        assertEquals("resource not found", jsonContext.read("$.errors[0]"),
                "incorrect error");

        testUser = userRepository.findByEmail("test_user_forgot@test.com");
        assertEquals("emptyResetToken", testUser.getResetToken());


        //  -- success --
        // setup
        MultiValueMap<String, String> paramsSuccess = new LinkedMultiValueMap<>();
        paramsSuccess.add("email", incorrectEmail);
        HttpEntity<MultiValueMap<String, String>> entityPostSuccess = new HttpEntity<>(paramsSuccess, headers);

        // call
        response = template
                .postForEntity(builder.toUriString(), entityPostSuccess, String.class);

        json = response.getBody();
        jsonContext = JsonPath.parse(json);

        // assertions response
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "incorrect http status code after successful call");
        assertEquals(jsonContext.read("$.email"), "test_user_forgot@test.com",
                "incorrect user return after successfull call");
        assertNull(jsonContext.read("$.resetToken"),
                        "resetToken not null after succesfull reset call");

        // assertions repository
        testUser = userRepository.findByEmail("test_user_forgot@test.com");
        assertNotNull(testUser.getResetToken(), "token null after successful call");

    }
}














