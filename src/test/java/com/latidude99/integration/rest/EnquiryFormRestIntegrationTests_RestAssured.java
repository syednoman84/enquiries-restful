package com.latidude99.integration.rest;

import com.latidude99.model.Enquiry;
import com.latidude99.service.AttachmentService;
import com.latidude99.service.EnquiryService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.URL;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;


/*
 * REST Assured only support csrf tokens sent as a part of the login page and not in headers
 * For this reason in csrf must be disabled for this test
 */

@Tag("slow")
//@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "/test.properties")
//@AutoConfigureMockMvc(secure=false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class EnquiryFormRestIntegrationTests_RestAssured {
    private URL baseUrl;

    @Autowired
    private EnquiryService enquiryService;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    CsrfTokenRepository csrfTokenRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    public void init() throws Exception{
        baseUrl = new URL("http://localhost:" + port);
    }

    @Order(1)
    @Test
    @DisplayName("EnquiryControllerRest - @/api/enquiry/form - checks if new Enquiry object is returned")
    public void enquiryFormRestTest_1() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        .when()
                .get(baseUrl.toString() + "/api/enquiry/form")
        .then()
                .statusCode(200)
                .body("name", is(nullValue()))
                .body("message", is(nullValue()));
    }

    @Order(2)
    @Test
    @DisplayName("EnquiryController - @/api/enquiry/form - checks response after enquiry form submitted ")
    @Commit
    @Sql(scripts = "/clear-and-load-data.sql", executionPhase = BEFORE_TEST_METHOD)
    public void enquiryFormTest_1() throws Exception {

        /*
         * MockMultipartFile parameter "name" (files) has to be the same as
         * "@RequestParam MultipartFile[] files" in the controller to be seen as an array
         */
        MockMultipartFile firstFile = new MockMultipartFile(
                "files",
                "jpg-file.jpg",
                "application/octet-stream",
                "src\\test\\resources\\multipart-test.jpg".getBytes());
        MockMultipartFile secondFile = new MockMultipartFile(
                "files",
                "xml-file.txt",
                "text/plain", "xml".getBytes());
        MockMultipartFile thirdFile = new MockMultipartFile(
                "files",
                "other-file-type.data",
                "text/plain", "some other type".getBytes());

 //     CsrfToken csrfToken = csrfTokenRepository.generateToken(null);

        String body = given()
//                  .header(csrfToken.getHeaderName(),  csrfToken.getToken()) // will not work
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                    .multiPart("files", "jpg-file.jpg", firstFile.getBytes())
                    .multiPart("files", "xml-file.txt", secondFile.getBytes())
                    .multiPart("files", "other-file-type.data", thirdFile.getBytes())
                    .formParam("name", "API Integration Test")
                    .formParam("email", "api_integration@test.com")
                    .formParam("phone", "1234567890")
                    .formParam("isbn", "")
                    .formParam("type", "Customised maps")
                    .formParam("message", "Spring Boot 2 layer test, form (api)")
                    .formParam("polygon",
                            "(-22.890469493606766, -43.17379993563645)") //not a real polygon
                    .formParam("polygonencoded",
                            "EflAroGtcAln@nwD`yC~zK}Fn`Gk}B`{@kcH_") //not a real encoded polygon
            .when()
                    .post(baseUrl.toString() + "/api/enquiry/form")
            .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .body("name", Matchers.is("API Integration Test"))
                    .body("email", Matchers.is("api_integration@test.com"))
                    .body("phone", Matchers.is("1234567890"))
                    .body("isbn", Matchers.is(""))
                    .body("type", Matchers.is("Customised maps"))
                    .body("message",
                            Matchers.is("Spring Boot 2 layer test, form (api)"))
                    .body("polygon",
                            Matchers.is("[ [-22.890469493606766, -43.17379993563645] ]"))
                    .body("polygonEncoded",
                            Matchers.is("EflAroGtcAln@nwD`yC~zK}Fn`Gk}B`{@kcH_"))
                    .extract()
                    .body()
                    .toString();
    }



    /*
     * Has to be run with the previous test1, asserts changes commited to database
     */

    @Order(3)
    @Test
    @DisplayName("EnquiryController - @/api/enquiry/form - checks enquiry repository entry")
    //@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)  // new test DB loaded after
    @Sql(scripts = "/clear-and-load-data.sql", executionPhase = AFTER_TEST_METHOD)
    public void enquiryFormTest_2() throws Exception {

        Enquiry enquiryPost = enquiryService.getLastByName("API Integration Test"); // entry from test1

        assertNotNull(enquiryPost, "submitted enquiry null");

        assertAll("enquiry properties",
                () -> assertEquals(19, enquiryService.getAll().size(), "incorrect enquiries number"),
                () -> assertEquals("API Integration Test", enquiryPost.getName(),
                "enquiryPost incorrect name property"),
                () -> assertEquals("api_integration@test.com", enquiryPost.getEmail(),
                "enquiryPost incorrect email property"),
                () -> assertEquals("1234567890", enquiryPost.getPhone(),
                "enquiryPost incorrect phone property"),
                () -> assertEquals("Customised maps", enquiryPost.getType(),
                "enquiryPost incorrect type property"),
                () -> assertEquals("Spring Boot 2 layer test, form (api)", enquiryPost.getMessage(),
                "enquiryPost incorrect message property"),
                () -> assertEquals("[ [-22.890469493606766, -43.17379993563645] ]", enquiryPost.getPolygon(),
                "enquiryPost incorrect polygon property"),  //  service method adds [  ]
                () -> assertEquals("EflAroGtcAln@nwD`yC~zK}Fn`Gk}B`{@kcH_",
                enquiryPost.getPolygonEncoded(),"enquiryPost incorrect polygonEncoded property"));

        assertAll("files/attachments",
                () -> assertEquals(3, attachmentService.getAll().size()),
                () -> assertEquals("jpg-file.jpg", enquiryPost.getAttachments().get(0).getName()),
                () -> assertEquals("xml-file.txt", enquiryPost.getAttachments().get(1).getName()),
                () -> assertEquals("other-file-type.data", enquiryPost.getAttachments().get(2).getName()));

    }

}



















