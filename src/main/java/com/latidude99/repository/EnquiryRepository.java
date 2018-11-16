package com.latidude99.repository;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.latidude99.model.Enquiry;
import com.latidude99.model.User;

@Repository
public interface EnquiryRepository extends JpaRepository<Enquiry, Long>{
	
	Enquiry findById(long id);
	
	List<Enquiry> findAll();
	
	List<Enquiry> findByIdIn(List<Long> list);
	
	List<Enquiry> findFirst100ByOrderByCreatedDateDesc();
	
	long count();
	Long countByClosingUser(User user);
	Long countByStatus(String status);
	Long countByClosingUserAndStatus(User user, String status);
	Long countByProgressUserAndStatus(User user, String status);
	
	List<Enquiry> findByStatus(String status);
	
	Page<Enquiry> findAll(Pageable pageable);
	
	//------------------------------search, regular---------------------------
		
	List<Enquiry> findAllByNameIgnoreCaseAndCreatedDateBetween(String name, ZonedDateTime startDate, ZonedDateTime endDate);
	List<Enquiry> findAllByEmailIgnoreCaseAndCreatedDateBetween(String email, ZonedDateTime startDate, ZonedDateTime endDate);
	List<Enquiry> findAllByPhoneIgnoreCaseAndCreatedDateBetween(String phone, ZonedDateTime startDate, ZonedDateTime endDate);
	List<Enquiry> findAllByMessageIgnoreCaseAndCreatedDateBetween(String message, ZonedDateTime startDate, ZonedDateTime endDate);
	List<Enquiry>  findAllByIsbnAndCreatedDateBetween(String isbn, ZonedDateTime endDate, ZonedDateTime startDate);
	
	List<Enquiry> findAllByCreatedDateBetweenAndNameIgnoreCaseContaining(ZonedDateTime startDate, ZonedDateTime endDate, String name);
	List<Enquiry> findAllByCreatedDateBetweenAndEmailIgnoreCaseContaining(ZonedDateTime startDate, ZonedDateTime endDate,String email);
	List<Enquiry> findAllByCreatedDateBetweenAndPhoneIgnoreCaseContaining(ZonedDateTime startDate, ZonedDateTime endDate, String phone);
	List<Enquiry> findAllByCreatedDateBetweenAndMessageIgnoreCaseContaining(ZonedDateTime startDate, ZonedDateTime endDate, String message);
	List<Enquiry> findAllByCreatedDateBetweenAndIsbnContaining(ZonedDateTime startDate, ZonedDateTime endDate, String isbn);
	
	List<Enquiry> findAllByClosingUserAndCreatedDateBetween(User closingUser, ZonedDateTime startDate, ZonedDateTime endDate);
	
	List<Enquiry> findAllByCreatedDateBetween(ZonedDateTime startDate, ZonedDateTime endDate);
}


















