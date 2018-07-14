package com.latidude99.model;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;



@Entity
public class User implements Serializable{
	private static final long serialVersionUID = 8404628707234409704L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
//	@NotEmpty(message = "{com.latidude99.model.User.name.NotEmpty}")
	private String name;
	
//	@NotEmpty(message = "{com.latidude99.model.User.lastName.NotEmpty}")
	private String lastName;
	
	@NotEmpty(message = "{com.latidude99.model.User.email.NotEmpty}")
	@Email(message = "{com.latidude99.model.User.email.Email}")
	private String email;
	
	@Size(min=6, message = "{com.latidude99.model.User.password.Size}")
	private String password;
	
	@Column(columnDefinition="BOOLEAN DEFAULT false")
	private boolean enabled;
	
	private String confirmationToken;
	private ZonedDateTime registered;
	
	@ManyToMany(cascade=CascadeType.PERSIST, fetch=FetchType.EAGER)
	private Set<UserRole> roles = new HashSet<>();
	
	@ManyToMany
    private List<Enquiry> enquiriesProgress = new ArrayList<>();
	
	@OneToMany(mappedBy = "closingUser", 
			fetch = FetchType.EAGER, 
			cascade = {CascadeType.MERGE, CascadeType.REMOVE }, 
			orphanRemoval = true)
	private List<Enquiry> enquiriesClosed = new ArrayList<>();
	
	
/*
	@PrePersist
	protected void onCreate() {
		registered = ZonedDateTime.now();
	  }

	public void addEnquiryProgress(Enquiry enquiry) {
        enquiry.setProgressUser(this);
        getEnquiriesProgress().add(enquiry);
    }
	
	public void addAllEnquiriesProgress(List<Enquiry> enquiries) {
		enquiries.forEach(enquiry -> enquiry.setUser(this));
        getEnquiriesProgress().addAll(enquiries);
    }
*/

	/* setters, getters and toString */
	

	public long getId() {
		return id;
	}



	public void setId(long id) {
		this.id = id;
	}



	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}



	public String getLastName() {
		return lastName;
	}



	public void setLastName(String lastName) {
		this.lastName = lastName;
	}



	public String getEmail() {
		return email;
	}



	public void setEmail(String email) {
		this.email = email;
	}



	public String getPassword() {
		return password;
	}



	public void setPassword(String password) {
		this.password = password;
	}



	public boolean isEnabled() {
		return enabled;
	}



	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}



	public String getConfirmationToken() {
		return confirmationToken;
	}



	public void setConfirmationToken(String confirmationToken) {
		this.confirmationToken = confirmationToken;
	}



	public ZonedDateTime getRegistered() {
		return registered;
	}



	public void setRegistered(ZonedDateTime registered) {
		this.registered = registered;
	}



	public Set<UserRole> getRoles() {
		return roles;
	}



	public void setRoles(Set<UserRole> roles) {
		this.roles = roles;
	}



	public List<Enquiry> getEnquiriesProgress() {
		return enquiriesProgress;
	}



	public void setEnquiriesProgress(List<Enquiry> enquiriesProgress) {
		this.enquiriesProgress = enquiriesProgress;
	}



	public List<Enquiry> getEnquiriesClosed() {
		return enquiriesClosed;
	}



	public void setEnquiriesClosed(List<Enquiry> enquiriesClosed) {
		this.enquiriesClosed = enquiriesClosed;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((confirmationToken == null) ? 0 : confirmationToken.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + ((enquiriesClosed == null) ? 0 : enquiriesClosed.hashCode());
		result = prime * result + ((enquiriesProgress == null) ? 0 : enquiriesProgress.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((registered == null) ? 0 : registered.hashCode());
		result = prime * result + ((roles == null) ? 0 : roles.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (confirmationToken == null) {
			if (other.confirmationToken != null)
				return false;
		} else if (!confirmationToken.equals(other.confirmationToken))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (enabled != other.enabled)
			return false;
		if (enquiriesClosed == null) {
			if (other.enquiriesClosed != null)
				return false;
		} else if (!enquiriesClosed.equals(other.enquiriesClosed))
			return false;
		if (enquiriesProgress == null) {
			if (other.enquiriesProgress != null)
				return false;
		} else if (!enquiriesProgress.equals(other.enquiriesProgress))
			return false;
		if (id != other.id)
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (registered == null) {
			if (other.registered != null)
				return false;
		} else if (!registered.equals(other.registered))
			return false;
		if (roles == null) {
			if (other.roles != null)
				return false;
		} else if (!roles.equals(other.roles))
			return false;
		return true;
	}



	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", lastName=" + lastName + ", email=" + email + ", password="
				+ password + ", enabled=" + enabled + ", confirmationToken=" + confirmationToken + ", registered="
				+ registered + ", roles=" + roles + ", enquiriesProgress=" + enquiriesProgress.size() + ", enquiriesClosed="
				+ enquiriesClosed.size() + "]";
	}
	  
	
	

}




























