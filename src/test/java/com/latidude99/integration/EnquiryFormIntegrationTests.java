package com.latidude99.integration;

import com.latidude99.model.Enquiry;
import com.latidude99.service.AttachmentService;
import com.latidude99.service.EnquiryService;
import com.latidude99.util.FormBean;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
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
 *  - @ActiveProfiles("test") - exclude AppConfig class that makes calls to
 *    database when starting the app and context wouldn't load with it
 *  - (secure=false) - to work around Spring Security authentication
 *  - tests Enquiry image property at http://localhost:8080/image/4 (test3)
 *  - uses pre-defined entries in DB, loaded from data.sql
 */

@Tag("slow")
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(locations = "/test.properties")
@AutoConfigureMockMvc(secure=false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class EnquiryFormIntegrationTests {

    static Enquiry enquiry = new Enquiry();
    static FormBean formBean = new FormBean();

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EnquiryService enquiryService;

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private MockMvc mockMvc;

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
    @DisplayName("EnquiryController - @/enquiry/form - submits enquiry form")
    @Commit
        @Sql(scripts = "/clear-and-load-data.sql", executionPhase = BEFORE_TEST_METHOD)
    public void enquiryFormTest_1() throws Exception {


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

        MvcResult result = this.mockMvc
                .perform(multipart("/enquiry/form")
                        .file(firstFile)
                        .file(secondFile)
                        .file(thirdFile)
                        .with(csrf())
                        .flashAttr("enquiry", enquiry)
                        .param("name", "Integration Test")
                        .param("email", "integration@test.com")
                        .param("phone", "1234567890")
                        .param("type", "Customised maps")
                        .param("message", "Spring Boot 2 integration test, form")
                        .param("polygon",
                                "[-22.890469493606766, -43.17379993563645]") //not real polygon
                        .param("polygonEncoded",
                                "EflAroGtcAln@nwD`yC~zK}Fn`Gk}B`{@kcH_")) //not real encoded polygon
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("enquirySubmit"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("uploadError", nullValue()))
                .andExpect(model().attribute("uploadErrorMessage", nullValue()))
                .andExpect(model().attribute("enquiry",
                        hasProperty("name", is("Integration Test"))))
                .andExpect(model().attribute("enquiry",
                        hasProperty("email", is("integration@test.com"))))
                .andExpect(model().attribute("enquiry",
                        hasProperty("phone", is("1234567890"))))
                .andExpect(model().attribute("enquiry",
                        hasProperty("type", is("Customised maps"))))
                .andExpect(model().attribute("enquiry",
                        hasProperty("message", is("Spring Boot 2 integration test, form"))))
                .andExpect(model().attribute("enquiry",
                        hasProperty("polygon", is("[-22.890469493606766, -43.17379993563645]"))))
                .andExpect(model().attribute("enquiry",
                        hasProperty("polygonEncoded", is("EflAroGtcAln@nwD`yC~zK}Fn`Gk}B`{@kcH_"))))
                .andReturn();

    }

    /*
     * Has to be run with the previous test1, checks changes commited to database
     */

    @Order(2)
    @Test
    @DisplayName("EnquiryController - @/enquiry/form - checks enquiry entry")
    //@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)  // new test DB loaded after
    @Sql(scripts = "/clear-and-load-data.sql", executionPhase = AFTER_TEST_METHOD)
    public void enquiryFormTest_2() throws Exception {

        Enquiry enquiryPost = enquiryService.getFirstByName("Integration Test"); // entry from test1

        assertNotNull(enquiryPost);

        assertAll("enquiry properties",
                () -> assertEquals(19, enquiryService.getAll().size(), "incorrect enquiries number"),
                () -> assertEquals("Integration Test", enquiryPost.getName(),
                "enquiryPost incorrect name property"),
                () -> assertEquals("integration@test.com", enquiryPost.getEmail(),
                "enquiryPost incorrect email property"),
                () -> assertEquals("1234567890", enquiryPost.getPhone(),
                "enquiryPost incorrect phone property"),
                () -> assertEquals("Customised maps", enquiryPost.getType(),
                "enquiryPost incorrect type property"),
                () -> assertEquals("Spring Boot 2 integration test, form", enquiryPost.getMessage(),
                "enquiryPost incorrect message property"),
                () -> assertEquals("[ [-22.890469493606766, -43.17379993563645] ]", enquiryPost.getPolygon(),
                "enquiryPost incorrect polygon property"),  //  service method adds [  ]
                () -> assertEquals("EflAroGtcAln@nwD`yC~zK}Fn`Gk}B`{@kcH_",
                enquiryPost.getPolygonEncoded(),"enquiryPost incorrect encodedPolygon property"));

        assertAll("files/attachments",
                () -> assertEquals(3, attachmentService.getAll().size()),
                () -> assertEquals("jpg-file.jpg", enquiryPost.getAttachments().get(0).getName()),
                () -> assertEquals("xml-file.txt", enquiryPost.getAttachments().get(1).getName()),
                () -> assertEquals("other-file-type.data", enquiryPost.getAttachments().get(2).getName()));

    }





}



























