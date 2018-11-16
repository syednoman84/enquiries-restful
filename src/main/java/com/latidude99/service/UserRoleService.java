package com.latidude99.service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.latidude99.model.Role;
import com.latidude99.model.User;
import com.latidude99.model.UserRole;
import com.latidude99.repository.UserRepository;
import com.latidude99.repository.UserRoleRepository;

@Service
public class UserRoleService {
	
	@Autowired
	private UserRoleRepository userRoleRepository;
	
	
	public UserRole getUserRole(String role) {
		return userRoleRepository.findByRole(role);
	}
	
	
}