package com.latidude99.layer;

import com.latidude99.model.*;
import com.latidude99.repository.UserRepository;
import com.latidude99.service.EnquiryService;
import com.latidude99.util.FormBean;
import com.latidude99.util.SearchWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

/*
 * Uses pre-defined DB entries in data.sql
 */
@Tag("medium")
@TestPropertySource(locations = "/test.properties")
@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import(EnquiryService.class)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
public class EnquiryServiceLayerTests {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    EnquiryService enquiryService;

    @Autowired
    UserRepository userRepository;

    @MockBean
    FormBean formBean;


    @Test
    @DisplayName("EnquiryService - get number by user assigned and status")
    public void enquiryServiceTest_1(){
        User user = userRepository.findByName("Lati Test");
        Long expectedNumber = new Long(6);
        Long actualNumber = enquiryService.getNumByProgressUserAndStatus(user, "in progress");

        assertEquals(expectedNumber, actualNumber, "incorrect number of users");
    }

    @Test
    @DisplayName("EnquiryService - saves enquiry comments")
    public void enquiryServiceTest_2(){
        String expectedComment = "this a test comment for enquiry service layer test";

        Mockito.when(formBean.getUserId()).thenReturn("1");
        Mockito.when(formBean.getEnquiryId()).thenReturn("5");
        Mockito.when(formBean.getCommentContent()).thenReturn(expectedComment);

        enquiryService.saveComment(formBean);

        List<Comment> comments = enquiryService.getById(5L).getComments();
        String actualComment = comments.get(comments.size() - 1).getContent();

        assertEquals(expectedComment, actualComment, "incorrect comment content");
    }

    @Test
    @DisplayName("EnquiryService - gets custom number of enquiries")
    public void enquiryServiceTest_3(){

        int expectedNumber = 12;
        int actualNumber= enquiryService.getLastUserDefined(0, 12).size();

        assertEquals(expectedNumber, actualNumber, "incorrect number of enquiries");
    }

    @Test
    @DisplayName("EnquiryService - gets custom number of enquiries")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void enquiryServiceTest_4(){

        int expectedSizeInProgress = 6;
        int expectedSizeInClosed = 0;

        User user = userRepository.findByName("Lati Test");
        int actualSizeInProgress = enquiryService.getByStatusAndUser("in progress", user).size();
        int actualSizeClosed = enquiryService.getByStatusAndUser("closed", user).size();

        assertEquals(expectedSizeInProgress, actualSizeInProgress,
                "incorrect number of enquiries 'in progress' assigne to the user");
        assertEquals(expectedSizeInClosed, actualSizeClosed,
                "incorrect number of enquiries 'closed' by the user");
    }

    @Test
    @DisplayName("EnquiryService - sorts users assign to an enquiry")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void enquiryServiceTest_5(){

        Enquiry enquiry1 = enquiryService.getById(9L);
        Enquiry enquiry2 = enquiryService.getById(12L);
        List<Enquiry> enquiries = new ArrayList<>();
        enquiries.add(enquiry1);
        enquiries.add(enquiry2);

        List<Enquiry> enquiriesAfter = enquiryService.sortProgressUsers(enquiries);

        assertEquals(2, enquiriesAfter.size(), "incorrect size of the returned List");

        Enquiry enquiry1After = enquiriesAfter.get(0);
        Enquiry enquiry2After = enquiriesAfter.get(1);

        assertAll("assigned users order with date",
                () ->assertEquals(9L, enquiry1After.getId(),
                        "incorrect enquiry1 number"),
                () ->assertEquals(3, enquiry1After.getSortedProgressUsersWithDate().size(),
                        "incorrect number of assigned users, enquiry1"),
                () -> assertEquals("Test (15.07.2018)", enquiry1After.getSortedProgressUsersWithDate().get(2),
                        "incorrect 1st user, enquiry1"),
                () -> assertEquals("Lati Test (26.07.2018)", enquiry1After.getSortedProgressUsersWithDate().get(1),
                        "incorrect 2nd user, enquiry1"),
                () -> assertEquals("Demo (02.08.2018)", enquiry1After.getSortedProgressUsersWithDate().get(0),
                "incorrect 3rd user, enquiry1"),

                () ->assertEquals(12L, enquiry2After.getId(),
                        "incorrect enquiry2 number"),
                () ->assertEquals(3, enquiry2After.getSortedProgressUsersWithDate().size(),
                        "incorrect number of assigned users, enquiry2"),
                () -> assertEquals("Test (18.07.2018)", enquiry2After.getSortedProgressUsersWithDate().get(2),
                        "incorrect 1st user, enquiry2"),
                () -> assertEquals("Lati Test (29.07.2018)", enquiry2After.getSortedProgressUsersWithDate().get(1),
                        "incorrect 2nd user, enquiry2"),
                () -> assertEquals("Demo (05.08.2018)", enquiry2After.getSortedProgressUsersWithDate().get(0),
                        "incorrect 3rd user, enquiry2"));
    }

    @Test
    @DisplayName("EnquiryService - updates enquiry status in the list")
    public void enquiryServiceTest_6(){

        List<Enquiry> enquiries = enquiryService.getAll();
        Enquiry enquiryToUpdate = enquiries.stream()
                .filter(e -> e.getId() == 1L)
                .collect(Collectors.toList())
                .get(0);

        assertEquals("waiting", enquiryToUpdate.getStatus(), "incorrect status before");

        enquiries = enquiryService.updateEnquiryListToView(enquiries, 1L, "in progress");
        Enquiry enquiryUpdated = enquiries.stream()
                .filter(e -> e.getId() == 1L)
                .collect(Collectors.toList())
                .get(0);

        assertEquals("in progress", enquiryUpdated.getStatus(), "incorrect status after");
    }

    @Test
    @DisplayName("EnquiryService - sorts enquiries by 'id'")
    @Sql(scripts = "/enquiry-service-layer-tests_7to13_add_enquiries.sql",
            executionPhase = BEFORE_TEST_METHOD) // 3 new enquiries
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void enquiryServiceTest_7(){

        List<Enquiry> enquiries = new ArrayList<>();
        enquiries.add(enquiryService.getFirstByName("Customer A"));
        enquiries.add(enquiryService.getFirstByName("Customer B"));
        enquiries.add(enquiryService.getFirstByName("Customer C"));

        assertAll("enquiries before",
                () -> assertEquals("Customer A", enquiries.get(0).getName(),
                        "incorrect order of the list before, customer A"),
                () -> assertEquals("Customer B", enquiries.get(1).getName(),
                        "incorrect order of the list before, customer B"),
                () -> assertEquals("Customer C", enquiries.get(2).getName(),
                        "incorrect order of the list before, customer C"));

        // after sorting by id 1st time - should be descending order
        // (sorted ascending before, when added to the list)
        List<Enquiry> enquiriesId1st = enquiryService.sortBy(enquiries, "id");
        assertAll("enquiries after 'id' 1st time",
                () -> assertEquals("Customer C", enquiriesId1st.get(0).getName(),
                        "incorrect order of the list after 'id' 1st, customer C"),
                () -> assertEquals("Customer B", enquiriesId1st.get(1).getName(),
                        "incorrect order of the list after 1st, customer B"),
                () -> assertEquals("Customer A", enquiriesId1st.get(2).getName(),
                        "incorrect order of the list after 1st, customer A"));

        // after sorting by id 2nd time - should be ascending order
        List<Enquiry> enquiriesId2nd = enquiryService.sortBy(enquiries, "id");
        assertAll("enquiries after 'id' 2nd time",
                () -> assertEquals("Customer A", enquiriesId2nd.get(0).getName(),
                        "incorrect order of the list after 2nd, customer A"),
                () -> assertEquals("Customer B", enquiriesId2nd.get(1).getName(),
                        "incorrect order of the list after 2nd, customer B"),
                () -> assertEquals("Customer C", enquiriesId2nd.get(2).getName(),
                        "incorrect order of the list after 2nd, customer C"));
    }

    @Test
    @DisplayName("EnquiryService - sorts enquiries by 'name'")
    @Sql(scripts = "/enquiry-service-layer-tests_7to13_add_enquiries.sql",
            executionPhase = BEFORE_TEST_METHOD) // 3 new enquiries
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void enquiryServiceTest_8(){

        List<Enquiry> enquiries = new ArrayList<>();
        enquiries.add(enquiryService.getFirstByName("Customer B"));
        enquiries.add(enquiryService.getFirstByName("Customer A"));
        enquiries.add(enquiryService.getFirstByName("Customer C"));

        assertAll("enquiries before",
                () -> assertEquals("Customer B", enquiries.get(0).getName(),
                        "incorrect order of the list before, customer B"),
                () -> assertEquals("Customer A", enquiries.get(1).getName(),
                        "incorrect order of the list before, customer A"),
                () -> assertEquals("Customer C", enquiries.get(2).getName(),
                        "incorrect order of the list before, customer C"));

        // after sorting by name 1st time - should be ascending order (not sorted before)
        List<Enquiry> enquiriesName1st = enquiryService.sortBy(enquiries, "name");
        assertAll("enquiries after 'name' 1st time",
                () -> assertEquals("Customer A", enquiriesName1st.get(0).getName(),
                        "incorrect order of the list after 'id' 1st, customer A"),
                () -> assertEquals("Customer B", enquiriesName1st.get(1).getName(),
                        "incorrect order of the list after, customer B"),
                () -> assertEquals("Customer C", enquiriesName1st.get(2).getName(),
                        "incorrect order of the list after, customer C"));

        // after sorting by name 2nd time - should be descending order
        List<Enquiry> enquiriesName2nd = enquiryService.sortBy(enquiries, "name");
        assertAll("enquiries after 'name' 2nd time",
                () -> assertEquals("Customer C", enquiriesName2nd.get(0).getName(),
                        "incorrect order of the list after 'id' 1st, customer C"),
                () -> assertEquals("Customer B", enquiriesName2nd.get(1).getName(),
                        "incorrect order of the list after, customer B"),
                () -> assertEquals("Customer A", enquiriesName2nd.get(2).getName(),
                        "incorrect order of the list after, customer A"));
    }

    @Test
    @DisplayName("EnquiryService - sorts enquiries by 'email'")
    @Sql(scripts = "/enquiry-service-layer-tests_7to13_add_enquiries.sql",
            executionPhase = BEFORE_TEST_METHOD) // 3 new enquiries
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void enquiryServiceTest_9(){

        List<Enquiry> enquiries = new ArrayList<>();
        enquiries.add(enquiryService.getFirstByName("Customer B"));
        enquiries.add(enquiryService.getFirstByName("Customer A"));
        enquiries.add(enquiryService.getFirstByName("Customer C"));

        assertAll("enquiries before",
                () -> assertEquals("Customer B", enquiries.get(0).getName(),
                        "incorrect order of the list before, customer B"),
                () -> assertEquals("Customer A", enquiries.get(1).getName(),
                        "incorrect order of the list before, customer A"),
                () -> assertEquals("Customer C", enquiries.get(2).getName(),
                        "incorrect order of the list before, customer C"));

        // after sorting by email 1st time - should be ascending order (not sorted before)
        List<Enquiry> enquiriesEmail1st = enquiryService.sortBy(enquiries, "email");
        assertAll("enquiries after 'email' 1st time",
                () -> assertEquals("Customer A", enquiriesEmail1st.get(0).getName(),
                        "incorrect order of the list after 1st, customer A"),
                () -> assertEquals("Customer B", enquiriesEmail1st.get(1).getName(),
                        "incorrect order of the list after 1st, customer B"),
                () -> assertEquals("Customer C", enquiriesEmail1st.get(2).getName(),
                        "incorrect order of the list after 1st, customer C"));

        // after sorting by email 2nd time - should be descending order
        List<Enquiry> enquiriesEmail2nd = enquiryService.sortBy(enquiries, "email");
        assertAll("enquiries after 'email' 2nd time",
                () -> assertEquals("Customer C", enquiriesEmail2nd.get(0).getName(),
                        "incorrect order of the list after 2nd, customer C"),
                () -> assertEquals("Customer B", enquiriesEmail2nd.get(1).getName(),
                        "incorrect order of the list after 2nd, customer B"),
                () -> assertEquals("Customer A", enquiriesEmail2nd.get(2).getName(),
                        "incorrect order of the list after 2nd, customer A"));
    }

    @Test
    @DisplayName("EnquiryService - sorts enquiries by 'type'")
    @Sql(scripts = "/enquiry-service-layer-tests_7to13_add_enquiries.sql",
            executionPhase = BEFORE_TEST_METHOD) // 3 new enquiries
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void enquiryServiceTest_10(){

        List<Enquiry> enquiries = new ArrayList<>();
        enquiries.add(enquiryService.getFirstByName("Customer A"));
        enquiries.add(enquiryService.getFirstByName("Customer B"));
        enquiries.add(enquiryService.getFirstByName("Customer C"));

        assertAll("enquiries before",
                () -> assertEquals("Customer A", enquiries.get(0).getName(),
                        "incorrect order of the list before, customer A"),
                () -> assertEquals("Customer B", enquiries.get(1).getName(),
                        "incorrect order of the list before, customer B"),
                () -> assertEquals("Customer C", enquiries.get(2).getName(),
                        "incorrect order of the list before, customer C"));

        // after sorting by type 1st time - should be ascening order (not sorted before)
        List<Enquiry> enquiriesType1st = enquiryService.sortBy(enquiries, "type");
        assertAll("enquiries after 'email' 1st time",
                () -> assertEquals("Customer C", enquiriesType1st.get(0).getName(),
                        "incorrect order of the list after 1st, customer C"),
                () -> assertEquals("Customer A", enquiriesType1st.get(1).getName(),
                        "incorrect order of the list after 1st, customer A"),
                () -> assertEquals("Customer B", enquiriesType1st.get(2).getName(),
                        "incorrect order of the list after1st, customer B"));

        // after sorting by type 2nd time - should be descending order
        List<Enquiry> enquiriesType2nd = enquiryService.sortBy(enquiries, "type");
        assertAll("enquiries after 'email' 2nd time",
                () -> assertEquals("Customer B", enquiriesType2nd.get(0).getName(),
                        "incorrect order of the list after 2nd, customer B"),
                () -> assertEquals("Customer A", enquiriesType2nd.get(1).getName(),
                        "incorrect order of the list after 2nd, customer A"),
                () -> assertEquals("Customer C", enquiriesType2nd.get(2).getName(),
                        "incorrect order of the list after 2nd, customer C"));
    }

    @Test
    @DisplayName("EnquiryService - sorts enquiries by 'created' date")
    @Sql(scripts = "/enquiry-service-layer-tests_7to13_add_enquiries.sql",
            executionPhase = BEFORE_TEST_METHOD) // 3 new enquiries
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void enquiryServiceTest_11(){

        List<Enquiry> enquiries = new ArrayList<>();
        enquiries.add(enquiryService.getFirstByName("Customer A"));
        enquiries.add(enquiryService.getFirstByName("Customer B"));
        enquiries.add(enquiryService.getFirstByName("Customer C"));

        assertAll("enquiries before",
                () -> assertEquals("Customer A", enquiries.get(0).getName(),
                        "incorrect order of the list before, customer A"),
                () -> assertEquals("Customer B", enquiries.get(1).getName(),
                        "incorrect order of the list before, customer B"),
                () -> assertEquals("Customer C", enquiries.get(2).getName(),
                        "incorrect order of the list before, customer C"));

        // after sorting by created 1st time - should be ascending order (not sorted before)
        List<Enquiry> enquiriesCreated1st = enquiryService.sortBy(enquiries, "created");
        assertAll("enquiries after 'created' 1st time",
                () -> assertEquals("Customer B", enquiriesCreated1st.get(0).getName(),
                        "incorrect order of the list after 1st, customer B"),
                () -> assertEquals("Customer C", enquiriesCreated1st.get(1).getName(),
                        "incorrect order of the list after 1st, customer C"),
                () -> assertEquals("Customer A", enquiriesCreated1st.get(2).getName(),
                        "incorrect order of the list after1st, customer A"));

        // after sorting by created 2nd time - should be descending order
        List<Enquiry> enquiriesCreated2nd = enquiryService.sortBy(enquiries, "created");
        assertAll("enquiries after 'created' 2nd time",
                () -> assertEquals("Customer A", enquiriesCreated2nd.get(0).getName(),
                        "incorrect order of the list after 2nd, customer A"),
                () -> assertEquals("Customer C", enquiriesCreated2nd.get(1).getName(),
                        "incorrect order of the list after 2nd, customer C"),
                () -> assertEquals("Customer B", enquiriesCreated2nd.get(2).getName(),
                        "incorrect order of the list after 2nd, customer B"));
    }

    @Test
    @DisplayName("EnquiryService - sorts enquiries by 'status'")
    @Sql(scripts = "/enquiry-service-layer-tests_7to13_add_enquiries.sql",
            executionPhase = BEFORE_TEST_METHOD) // 3 new enquiries
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void enquiryServiceTest_12(){

        List<Enquiry> enquiries = new ArrayList<>();
        enquiries.add(enquiryService.getFirstByName("Customer A"));
        enquiries.add(enquiryService.getFirstByName("Customer B"));
        enquiries.add(enquiryService.getFirstByName("Customer C"));

        assertAll("enquiries before",
                () -> assertEquals("Customer A", enquiries.get(0).getName(),
                        "incorrect order of the list before, customer A"),
                () -> assertEquals("Customer B", enquiries.get(1).getName(),
                        "incorrect order of the list before, customer B"),
                () -> assertEquals("Customer C", enquiries.get(2).getName(),
                        "incorrect order of the list before, customer C"));

        // after sorting by status 1st time - should be ascending order (sorted descending before)
        List<Enquiry> enquiriesStatus1st = enquiryService.sortBy(enquiries, "status");
        assertAll("enquiries after 'status' 1st time",
                () -> assertEquals("Customer C", enquiriesStatus1st.get(0).getName(),
                        "incorrect order of the list after 1st, customer C"),
                () -> assertEquals("Customer B", enquiriesStatus1st.get(1).getName(),
                        "incorrect order of the list after 1st, customer B"),
                () -> assertEquals("Customer A", enquiriesStatus1st.get(2).getName(),
                        "incorrect order of the list after1st, customer A"));

        // after sorting by status 2nd time - should be descending order
        List<Enquiry> enquiriesStatus2nd = enquiryService.sortBy(enquiries, "status");
        assertAll("enquiries after 'status' 2nd time",
                () -> assertEquals("Customer A", enquiriesStatus2nd.get(0).getName(),
                        "incorrect order of the list after 2nd, customer A"),
                () -> assertEquals("Customer B", enquiriesStatus2nd.get(1).getName(),
                        "incorrect order of the list after 2nd, customer B"),
                () -> assertEquals("Customer C", enquiriesStatus2nd.get(2).getName(),
                        "incorrect order of the list after 2nd, customer C"));
    }

    @Test
    @DisplayName("EnquiryService - sorts enquiries by 'closed' date")
    @Sql(scripts = "/enquiry-service-layer-tests_7to13_add_enquiries.sql",
            executionPhase = BEFORE_TEST_METHOD) // 3 new enquiries
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void enquiryServiceTest_13(){

        List<Enquiry> enquiries = new ArrayList<>();
        enquiries.add(enquiryService.getFirstByName("Customer A"));
        enquiries.add(enquiryService.getFirstByName("Customer B"));
        enquiries.add(enquiryService.getFirstByName("Customer C"));

        assertAll("enquiries before",
                () -> assertEquals("Customer A", enquiries.get(0).getName(),
                        "incorrect order of the list before, customer A"),
                () -> assertEquals("Customer B", enquiries.get(1).getName(),
                        "incorrect order of the list before, customer B"),
                () -> assertEquals("Customer C", enquiries.get(2).getName(),
                        "incorrect order of the list before, customer C"));

        // after sorting by closed 1st time - should be descending order (sorted ascending before)
        List<Enquiry> enquiriesClosed1st = enquiryService.sortBy(enquiries, "closed");
        assertAll("enquiries after 'status' 1st time",
                () -> assertEquals("Customer C", enquiriesClosed1st.get(0).getName(),
                        "incorrect order of the list after 1st, customer C"),
                () -> assertEquals("Customer A", enquiriesClosed1st.get(1).getName(),
                        "incorrect order of the list after 1st, customer A"),
                () -> assertEquals("Customer B", enquiriesClosed1st.get(2).getName(),
                        "incorrect order of the list after 1st, customer B"));

        // after sorting by closed 2nd time - should be ascending order
        List<Enquiry> enquiriesClosed2nd = enquiryService.sortBy(enquiries, "closed");
        assertAll("enquiries after 'status' 2nd time",
                () -> assertEquals("Customer A", enquiriesClosed2nd.get(0).getName(),
                        "incorrect order of the list after 2nd, customer A"),
                () -> assertEquals("Customer B", enquiriesClosed2nd.get(1).getName(),
                        "incorrect order of the list after 2nd, customer B"),
                () -> assertEquals("Customer C", enquiriesClosed2nd.get(2).getName(),
                        "incorrect order of the list after 2nd, customer C"));
    }

    @Test
    @DisplayName("EnquiryService - regular enquiry properties search")
    @Sql(scripts = "/enquiry-service-layer-test_14_add_enquiries.sql",
            executionPhase = BEFORE_TEST_METHOD)
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void enquiryServiceTest_14(){

        SearchWrapper searchWrapper = new SearchWrapper();
        searchWrapper.setSelector("exact");
        searchWrapper.setSearchFor("Customer A");
        searchWrapper.setSearchIn("");
        searchWrapper.setAssignedUser("any user");
        searchWrapper.setClosingUser("any user");
        searchWrapper.setStatus("all");
        searchWrapper.setLimit(100);

        List<Enquiry> enquiries = enquiryService.searchRegularResultListWithProgressUserList(searchWrapper);
        assertEquals("a.cust@gmail.com", enquiries.get(0).getEmail(),
                "should find 1 enquiry, 'exact' search");

        searchWrapper.setSearchFor(".cust*");
        enquiries = enquiryService.searchRegularResultListWithProgressUserList(searchWrapper);
        assertEquals(8, enquiries.size(),
                "should find all 8 enquiries, wildcard search");

        searchWrapper.setSearchFor(".cust*");
        searchWrapper.setLimit(5);
        enquiries = enquiryService.searchRegularResultListWithProgressUserList(searchWrapper);
        assertEquals(5, enquiries.size(),
                "should find 5 enquiries, wildcard search, limit 5");

        searchWrapper.setSearchFor(".cust*");
        searchWrapper.setStatus("closed");
        enquiries = enquiryService.searchRegularResultListWithProgressUserList(searchWrapper);
        assertEquals(3, enquiries.size(),
                "should find 3 enquiries, wildcard search, status 'closed'");

        searchWrapper.setSearchFor("th enquiry");
        searchWrapper.setSearchIn("in customer messages");
        searchWrapper.setStatus("all");
        enquiries = enquiryService.searchRegularResultListWithProgressUserList(searchWrapper);
        assertEquals(0, enquiries.size(),
                "should find 0 enquiries, exact search, in messages");

        searchWrapper.setSearchFor("th enquiry*");
        searchWrapper.setSearchIn("in customer messages");
        enquiries = enquiryService.searchRegularResultListWithProgressUserList(searchWrapper);
        assertEquals(5, enquiries.size(),
                "should find 5 enquiries, wildcard search, in messages");

        searchWrapper.setSearchFor("");
        searchWrapper.setSearchIn("");
        searchWrapper.setDateRange("01/01/2017 - 01/01/2018");
        enquiries = enquiryService.searchRegularResultListWithProgressUserList(searchWrapper);
        assertEquals(5, enquiries.size(),
                "should find 5 enquiries, exact search, date range");

        searchWrapper.setSearchFor("e.*");
        searchWrapper.setSearchIn("in customer emails");
        enquiries = enquiryService.searchRegularResultListWithProgressUserList(searchWrapper);
        assertEquals(1, enquiries.size(),
                "should find 1 enquiry, wildcard search, date range");

        searchWrapper.setSearchFor("e.cust*");
        searchWrapper.setSearchIn("");
        searchWrapper.setAssignedUser("Demo");
        enquiries = enquiryService.searchRegularResultListWithProgressUserList(searchWrapper);
        assertEquals(0, enquiries.size(),
                "should find 0 enquiries, wildcard search, date range, assigned user Demo");

        searchWrapper.setSearchFor("e.cust*");
        searchWrapper.setSearchIn("");
        searchWrapper.setClosingUser("Demo");
        enquiries = enquiryService.searchRegularResultListWithProgressUserList(searchWrapper);
        assertEquals(0, enquiries.size(),
                "should find  enquiries, wildcard search, date range, closing user Demo");


    }

}



















