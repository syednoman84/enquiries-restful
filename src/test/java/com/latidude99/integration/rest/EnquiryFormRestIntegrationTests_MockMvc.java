package com.latidude99.integration.rest;

import com.latidude99.model.Enquiry;
import com.latidude99.service.AttachmentService;
import com.latidude99.service.EnquiryService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/*
 *  - Mock server
 *    database when starting the app and context wouldn't load with it
 *  - (secure=false) - to work around Spring Security authentication
 *  - tests Enquiry image property at http://localhost:8080/image/4 (test3)
 *  - uses pre-defined entries in DB, loaded from data.sql
 */

@Tag("slow")
//@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "/test.properties")
@AutoConfigureMockMvc(secure=false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class EnquiryFormRestIntegrationTests_MockMvc {

    @Autowired
    private EnquiryService enquiryService;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    CsrfTokenRepository csrfTokenRepository;

    @BeforeAll
    public static void initAll(){
    }

    @AfterAll
    public static void tearDownAll(){
    }

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }


    @Order(1)
    @Test
    @DisplayName("EnquiryControllerRest - @/api/enquiry/form - checks response after enquiry form submitted ")
    @Commit
    @Sql(scripts = "/clear-and-load-data.sql", executionPhase = BEFORE_TEST_METHOD)
    public void enquiryFormRestTest_1() throws Exception {

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

        this.mockMvc
                .perform(multipart("/api/enquiry/form")
                        .file(firstFile)
                        .file(secondFile)
                        .file(thirdFile)
                        .with(csrf())
                        .param("name", "Noman API Integration Test")
                        .param("email", "api_integration@test.com")
                        .param("phone", "1234567890")
                        .param("isbn", "")
                        .param("type", "Customised maps")
                        .param("message", "Spring Boot 2 layer test, form (api)")
                        .param("polygon",
                                "(-22.890469493606766, -43.17379993563645)") //not a real polygon
                        .param("polygonencoded",
                                "EflAroGtcAln@nwD`yC~zK}Fn`Gk}B`{@kcH_")) //not a real encoded polygon
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", Matchers.is("Noman API Integration Test")))
                .andExpect(jsonPath("$.email", Matchers.is("api_integration@test.com")))
                .andExpect(jsonPath("$.phone", Matchers.is("1234567890")))
                .andExpect(jsonPath("$.isbn", Matchers.is("")))
                .andExpect(jsonPath("$.type", Matchers.is("Customised maps")))
                .andExpect(jsonPath("$.message",
                        Matchers.is("Spring Boot 2 layer test, form (api)")))
                .andExpect(jsonPath("$.polygon",
                        Matchers.is("[ [-22.890469493606766, -43.17379993563645] ]")))
                .andExpect(jsonPath("$.polygonEncoded",
                        Matchers.is("EflAroGtcAln@nwD`yC~zK}Fn`Gk}B`{@kcH_")))
                ;
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
                () -> assertEquals("Noman API Integration Test", enquiryPost.getName(),
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

























