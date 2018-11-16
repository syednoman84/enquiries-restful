package com.latidude99.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.latidude99.model.UserRole;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long>{
	
	UserRole findByRole(String role);

}
