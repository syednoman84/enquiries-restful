package com.latidude99.integration;

import com.latidude99.model.Comment;
import com.latidude99.model.Enquiry;
import com.latidude99.model.User;
import com.latidude99.repository.EnquiryRepository;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("slow")
@TestPropertySource(locations = "/test.properties")
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class EnquiryRepositoryTests {

    @Autowired
    EnquiryRepository enquiryRepository;

    @Test
    @DisplayName("EnquiryRepository test - number of enquiries")
    public void enquiryTest_1(){
        int expectedEnquiryNumber = 18;

        List<Enquiry> enquiryList = enquiryRepository.findAll();
        int actualEnquiryNumber = enquiryList.size();

        assertEquals(expectedEnquiryNumber, actualEnquiryNumber, "incorrect number of enquiries");
    }

    @Test
    @DisplayName("EnquiryRepository, testing basic properties")
    public void enquiryTest_2(){
        Enquiry expectedEnquiry = new Enquiry();
        expectedEnquiry.setName("Kris Triggle");
        expectedEnquiry.setEmail("kris.triggle@googlemail.com");
        expectedEnquiry.setIsbn("978887645676");
        expectedEnquiry.setMessage("I am looking for a map of the Stroud parliamentary constituency" +
                " showing the borders of the constituency and showing other areas in grey scale. " +
                "Is this or something similar a product that you could provide?\n" + "\n" +
                "If so please could you let me know the approximate cost. I am ideally looking " +
                "for something in A1 size and will probably need at least two copies\n" + "\n" +
                "Please let me know there is any further information that would be helpful\n" + "\n" +
                "Many thanks in advance\n" + "\n" + "Kris  \n" + " \n");
        expectedEnquiry.setPhone("23456789876");
        expectedEnquiry.setPolygon("[ [42.85872929577766, -80.58923912822434],[43.099856781745004, " +
                "-79.18298912822434],[42.810390341169196, -78.54578209697434],[42.097078046999414, " +
                "-79.42468834697434],[41.26013214943742, -82.30310631572434],[41.54032330999431, " +
                "-83.33582115947434],[42.19482617765736, -83.77527428447434],[42.324922200918174, " +
                "-83.31384850322434],[42.74587958990549, -81.57800865947434] ]");
        expectedEnquiry.setStatus("waiting");
        expectedEnquiry.setType("Customised Mapping");

        Enquiry actualEnquiry = enquiryRepository.findById(5);

        assertEquals(
                expectedEnquiry.getName(), actualEnquiry.getName(), "enquiry name incorrect");
        assertEquals(
                expectedEnquiry.getEmail(), actualEnquiry.getEmail(), "enquiry email incorrect");
        assertEquals(expectedEnquiry.getPhone(), actualEnquiry.getPhone(), "enquiry phone incorrect");
        assertEquals(expectedEnquiry.getMessage(), actualEnquiry.getMessage(), "enquiry message incorrect");
        assertEquals(expectedEnquiry.getIsbn(), actualEnquiry.getIsbn(), "enquiry isbn incorrect");
        assertEquals(expectedEnquiry.getPolygon(), actualEnquiry.getPolygon(), "enquiry polygon incorrect");
        assertEquals(expectedEnquiry.getStatus(), actualEnquiry.getStatus(), "enquiry status incorrect");
        assertEquals(expectedEnquiry.getType(), actualEnquiry.getType(), "enquiry type incorrect");

    }

    @Test
    @DisplayName("EnquiryRepository, testing collection properties")
    public void enquiryTest_3(){

        Enquiry actualEnquiry1 = enquiryRepository.findById(5);
        Enquiry actualEnquiry2 = enquiryRepository.findById(7);

        assertEquals(0, actualEnquiry1.getComments().size());
        assertEquals(2, actualEnquiry2.getProgressUser().size());
    }

}


