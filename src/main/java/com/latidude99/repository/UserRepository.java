package com.latidude99.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.latidude99.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
	
	User findByEmail(String email);

	User findByName(String name);
	
	User findByConfirmationToken(String confirmationToken);

	User findById(long id);
	
	List<User> findAll();	

}
