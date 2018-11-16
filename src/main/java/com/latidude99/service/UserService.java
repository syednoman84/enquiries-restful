package com.latidude99.service;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.metadata.GenericTableMetaDataProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.latidude99.model.Role;
import com.latidude99.model.User;
import com.latidude99.model.UserRole;
import com.latidude99.repository.UserRepository;
import com.latidude99.repository.UserRoleRepository;

@Service
public class UserService {
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);
	
	private UserRepository userRepository;
	
	private UserRoleRepository roleRepository;

	@Autowired
	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	@Autowired
	public void setRoleRepository(UserRoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	public User getUserByUsername(String username) {
		User user = userRepository.findByEmail(username);
		return user;
	}
	
	public User save(User user) {
		userRepository.save(user);
		return user;
	}
	
	public List<User> getAll(){
		return  userRepository.findAll();
	}
	
	public List<User> getAllSinCurrent(User user){
		List<User> users = userRepository.findAll();
		users.remove(user);
		return users;
	}
	
	public User findByActivationToken(String activationToken) {
		return userRepository.findByActivationToken(activationToken);
	}
	
	public User findByResetToken(String activationToken) {
		return userRepository.findByResetToken(activationToken);
	}

	public User findById(long id) {
		return userRepository.findById(id);
	}
	
		
	public void addDbUser(Role role) {
		UserRole dbRole = roleRepository.findByRole(role.getText());
		Set<UserRole> dbRoles = new HashSet<>();
		dbRoles.add(dbRole);
		List<User> users = userRepository.findByRoles(dbRoles);
	    ZonedDateTime currentZonedDateTime = ZonedDateTime.now();
	    if(users != null) {
	    	for(User user :users) {
	    		user.setRegistered(currentZonedDateTime);
	    	    user.getRoles().add(dbRole);
	    	    String password = user.getPassword();
	    	    user.setPassword(passwordEncoder.encode(password));
	    	    user.setEnabled(true);
	    		userRepository.save(user);
	    		logger.debug(user.getEmail() + ",roles: " + user.getRoles().toString() + " - user pasword encrypted");
	    	}
	    }
	}
	
	public void addWithDefaultRole(User user) {
		UserRole defaultRole = roleRepository.findByRole(Role.DEFAULT.getText());
		user.getRoles().add(defaultRole);
		userRepository.save(user);
	}
	
	public void addWithAdminRole(User user) {
		UserRole defaultRole = roleRepository.findByRole(Role.ADMIN.getText());
		user.getRoles().add(defaultRole);
		userRepository.save(user);
	}
	
	public boolean isNameAvailable(User user) {
		if(userRepository.findByName(user.getName()) == null)
			return true;
		return false;
	}
	
	public boolean isEmailAvailable(User user) {
		if(userRepository.findByEmail(user.getEmail()) == null)
			return true;
		return false;
	}
	
	public List<String> getUserListAsStringList(){
		List<String> userList = getAll().stream()
				.map(user -> user.getName())
				.collect(Collectors.toList());
		userList.add("any user");
		return userList;
	}
	
	public void trimUserEmail(User user) {
		User userEmailTrimmed = userRepository.findById(user.getId());
		userEmailTrimmed.setEmail(userEmailTrimmed.getEmail().trim());
		userRepository.save(userEmailTrimmed);
	}
	
	
	
	
	
	
}


/*
	public void addWithDefaultRole(User user) {
//		ZonedDateTime currentZonedDateTime = ZonedDateTime.now();
//		user.setZonedTime(currentZonedDateTime);
		String password = user.getPassword();
		user.setPassword(passwordEncoder.encode(password));
		UserRole defaultRole = roleRepository.findByRole(DEFAULT_ROLE);
		user.getRoles().add(defaultRole);
		userRepository.save(user);
		System.out.println("user--> " + user);
	}
	
	public boolean isAvailable(User user) {
		if(userRepository.findByEmail(user.getEmail()) != null){
			return false;
		}else {
			return true;
		}
		
	}
	
	public void addAdmin() {
		User userAdmin = userRepository.findByEmail("latidude99test@gmail.com");
	    UserRole adminRole = roleRepository.findByRole(ADMIN_ROLE);
//	    UserRole defaultRole = roleRepository.findByRole(DEFAULT_ROLE);
	//    List<Contact> adminContacts = contactRepository.findByUserId(userAdmin.getId());
//	    System.out.println("Contacts (started) before adding to adminUser--> " + adminContacts);
	    ZonedDateTime currentZonedDateTime = ZonedDateTime.now();
		userAdmin.setRegistered(currentZonedDateTime);
		userAdmin.getRoles().add(adminRole);
//		userAdmin.getRoles().add(defaultRole);
	//	userAdmin.addAllContacts(adminContacts);
//	    userAdmin.getContacts().forEach(c -> c.setCreated(currentZonedDateTime));
		String password = userAdmin.getPassword();
		userAdmin.setPassword(passwordEncoder.encode(password));
		userAdmin.setEnabled(true);
//		userAdmin.getContacts().addAll(adminContacts);
//		System.out.println("userAdmin (started) --> " + userAdmin);
		userRepository.save(userAdmin);
	}
	
}

*/
/*	
	public void addUser(String name) {
		User userDemo = userRepository.findByFirstName(name.toLowerCase());
	    UserRole defaultRole = roleRepository.findByRole(DEFAULT_ROLE);
	  //  List<Contact> demoContacts = contactRepository.findByUserId(userDemo.getId());
	 //   System.out.println("Contacts (started) before adding to userDemo--> " + demoContacts);
	    ZonedDateTime currentZonedDateTime = ZonedDateTime.now();
		userDemo.setRegistered(currentZonedDateTime);
	    userDemo.getRoles().add(defaultRole);
	 //   userDemo.addAllContacts(demoContacts);
//	    userDemo.getContacts().forEach(c -> c.setCreated(currentZonedDateTime));
	    String password = userDemo.getPassword();
		userDemo.setPassword(passwordEncoder.encode(password));
		userDemo.setEnabled(true);
//		userAdmin.getContacts().addAll(adminContacts);
//		System.out.println("userDemo (started) --> " + userDemo);
		userRepository.save(userDemo);
	}
*/	
/*	public void updateDetails(User user) {
		userRepository;
	}
	
		public List<User> getAllUsersNoAdmins(){
			List<User> usersAll = userRepository.findAll();
			logger.info("usersAll size: " + usersAll.size() + ", " + usersAll.get(0).getRoles().toString());
			logger.info("usersAll size: " + usersAll.size() +  ", " + usersAll.get(1).getRoles().toString());
			UserRole defaultRole = roleRepository.findByRole(DEFAULT_ROLE);
			List<User> usersRoleUser = usersAll.stream()
					.filter(u -> u.getRoles().contains(defaultRole))
					.collect(Collectors.toList());
			logger.info("usersRoleUser size: " + usersRoleUser.size());
			return usersRoleUser;
		}
*/


















