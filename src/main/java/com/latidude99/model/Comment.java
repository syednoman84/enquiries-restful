package com.latidude99.model;

import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Comment implements Serializable{
	private static final long serialVersionUID = 4796718673528882031L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	private Long userId;
	private String userName;
	private String content;
	private ZonedDateTime date;
	
	
	@ManyToOne
	@JoinColumn(name = "enquiry_id")
	private Enquiry enquiry;
	
	public Comment(){}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public ZonedDateTime getDate() {
		return date;
	}

	public void setDate(ZonedDateTime date) {
		this.date = date;
	}

	public Enquiry getEnquiry() {
		return enquiry;
	}

	public void setEnquiry(Enquiry enquiry) {
		this.enquiry = enquiry;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
		
	
		

}
