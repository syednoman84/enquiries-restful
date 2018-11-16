package com.latidude99.model;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Entity
public class MailingList implements Serializable{
	private static final long serialVersionUID = 1344333039135354756L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	@NotEmpty(message = "{com.latidude99.model.Enquiry.name.NotEmpty}")
	private String name;
	
	@Email(message = "{com.latidude99.model.Enquiry.email.Email}")
	private String email;
	
	private ZonedDateTime added;
	
	private Integer sentNumber;
	
	public MailingList() {
		added = ZonedDateTime.now();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public ZonedDateTime getAdded() {
		return added;
	}

	public void setAdded(ZonedDateTime added) {
		this.added = added;
	}

	public Integer getSentNumber() {
		return sentNumber;
	}

	public void setSentNumber(Integer sentNumber) {
		this.sentNumber = sentNumber;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((added == null) ? 0 : added.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((sentNumber == null) ? 0 : sentNumber.hashCode());
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
		MailingList other = (MailingList) obj;
		if (added == null) {
			if (other.added != null)
				return false;
		} else if (!added.equals(other.added))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (sentNumber == null) {
			if (other.sentNumber != null)
				return false;
		} else if (!sentNumber.equals(other.sentNumber))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MailingList [id=" + id + ", name=" + name + ", email=" + email + ", added=" + added + ", sentNumber="
				+ sentNumber + "]";
	}
	
	

}
