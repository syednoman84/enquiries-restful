package com.latidude99.integration.rest;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.latidude99.model.Enquiry;
import com.latidude99.service.AttachmentService;
import com.latidude99.service.EnquiryService;
import com.latidude99.util.FileUploadResource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@Tag("slow")
//@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "/test.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EnquiryFormRestIntegrationTests_RestTemplate {

    @Autowired
    private EnquiryService enquiryService;

    @Autowired
    private AttachmentService attachmentService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    CsrfTokenRepository csrfTokenRepository;


    @Order(1)
    @Test
    @DisplayName("EnquiryControllerRest - @/api/enquiry/form - checks response after enquiry form submitted ")
    @Commit
    @Sql(scripts = "/clear-and-load-data.sql", executionPhase = BEFORE_TEST_METHOD)
    public void enquiryFormRestTest_1() throws Exception {

        // setup
        /*
         * MockMultipartFile parameter "name" (files) has to be the same as
         * "@RequestParam MultipartFile[] files" in the controller to be seen as an array
         */
        MockMultipartFile firstFile = new MockMultipartFile(
                "files", "jpg-file.jpg", "application/octet-stream",
                "src\\test\\resources\\multipart-test.jpg".getBytes());
        MockMultipartFile secondFile = new MockMultipartFile(
                "files", "xml-file.txt", "text/plain", "xml".getBytes());
        MockMultipartFile thirdFile = new MockMultipartFile(
                "files", "other-file-type.data", "text/plain", "some other type".getBytes());

        // file/resource has to have a name, otherwise will not be seen as part of the request
        ByteArrayResource contentsAsResource = new ByteArrayResource(firstFile.getBytes()) {
            @Override
            public String getFilename() {
                return "jpg-file.jpg";
            }
        };

        URL baseUrl = new URL("http://localhost:" + port);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        CsrfToken csrfToken = csrfTokenRepository.generateToken(null);
        headers.add(csrfToken.getHeaderName(), csrfToken.getToken());
        headers.add("Cookie", "XSRF-TOKEN=" + csrfToken.getToken());

        URI uri = UriComponentsBuilder
                .fromHttpUrl(baseUrl.toString())
                .path("/api/enquiry/form")
                .build().toUri();

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("name", "API Integration Test");
        params.add("email", "api_integration@test.com");
        params.add("phone", "1234567890");
        params.add("isbn", "");
        params.add("type", "Customised maps");
        params.add("message", "Spring Boot 2 layer test, form (api)");
        params.add("polygon", "(-22.890469493606766, -43.17379993563645)");
        params.add("polygonencoded", "EflAroGtcAln@nwD`yC~zK}Fn`Gk}B`{@kcH_");
        params.add("files",contentsAsResource);
        params.add("files",
                new FileUploadResource(secondFile.getBytes(), "xml-file.txt")); // helper class
        params.add("files",
                new FileUploadResource(thirdFile.getBytes(), "other-file-type.data")); // helper class

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(params, headers);


        // call
        ResponseEntity<String> response = template
                .exchange(uri, HttpMethod.POST, entity, String.class);

        String json = response.getBody();
        DocumentContext jsonContext = JsonPath.parse(json);

        // assertions
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("API Integration Test", jsonContext.read("$['name']"));
        assertEquals("api_integration@test.com", jsonContext.read("$['email']"));
        assertEquals("1234567890", jsonContext.read("$['phone']"));
        assertEquals("", jsonContext.read("$['isbn']"));
        assertEquals("Customised maps", jsonContext.read("$['type']"));
        assertEquals("Spring Boot 2 layer test, form (api)",
                jsonContext.read("$['message']"));
        assertEquals("[ [-22.890469493606766, -43.17379993563645] ]",
                jsonContext.read("$['polygon']"));
        assertEquals("EflAroGtcAln@nwD`yC~zK}Fn`Gk}B`{@kcH_",
                jsonContext.read("$['polygonEncoded']"));
        assertEquals(3, (int)jsonContext.read("$.attachmentsNumber"));
    }


    /*
     * Has to be run with the previous test1, asserts changes committed to database
     */
    @Order(2)
    @Test
    @DisplayName("EnquiryControllerRest - @/api/enquiry/form - checks enquiry repository entry")
    //@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)  // new test DB loaded after
    @Sql(scripts = "/clear-and-load-data.sql", executionPhase = AFTER_TEST_METHOD)
    public void enquiryFormRestTest_2() throws Exception {

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











