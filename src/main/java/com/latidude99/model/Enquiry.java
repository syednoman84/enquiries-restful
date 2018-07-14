package com.latidude99.model;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;



@Entity
public class Enquiry implements Serializable{
	private static final long serialVersionUID = -4646260725584082081L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	@NotEmpty(message = "{com.latidude99.model.Enquiry.name.NotEmpty}")
	private String name;
	
	@Email(message = "{com.latidude99.model.Enquiry.email.Email}")
	private String email;
	
	@NotEmpty(message = "{com.latidude99.model.Enquiry.message.NotEmpty}")
	@Size(max=2048)
	private String message;
	
	@Lob
	private byte[] attachment1;
	@Lob
	private byte[] attachment2;
	@Lob
	private byte[] attachment3;
	
	private ZonedDateTime created;
	
	private ZonedDateTime closedDate;
	
	@OneToMany(mappedBy = "enquiry", 
			fetch = FetchType.EAGER, 
			cascade = {CascadeType.MERGE, CascadeType.REMOVE }, 
			orphanRemoval = true)
	private List<Point> point;
	
	@ElementCollection
	@CollectionTable(name="emails_enquiry", joinColumns=@JoinColumn(name="enquiry_id"))
	@Column(name="emails_fwd")
	private List<String> emailsFwd;
	
	@ManyToMany(mappedBy = "enquiriesProgress")
    private List<User> progressUser = new ArrayList<>();
	
	@ManyToOne
    @JoinColumn(name = "user_closing_id")
    private User closingUser;
	
	
	
	@PrePersist
	protected void onCreate() {
	    created = ZonedDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {

	}
	
	
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<Point> getPoint() {
		return point;
	}

	public void setPoint(List<Point> point) {
		this.point = point;
	}

	public List<String> getEmailsFwd() {
		return emailsFwd;
	}

	public void setEmailsFwd(List<String> emailsFwd) {
		this.emailsFwd = emailsFwd;
	}

	public byte[] getAttachment1() {
		return attachment1;
	}

	public void setAttachment1(byte[] attachment1) {
		this.attachment1 = attachment1;
	}

	public byte[] getAttachment2() {
		return attachment2;
	}

	public void setAttachment2(byte[] attachment2) {
		this.attachment2 = attachment2;
	}

	public byte[] getAttachment3() {
		return attachment3;
	}

	public void setAttachment3(byte[] attachment3) {
		this.attachment3 = attachment3;
	}

	public ZonedDateTime getCreated() {
		return created;
	}

	public void setCreated(ZonedDateTime created) {
		this.created = created;
	}

	public ZonedDateTime getClosedDate() {
		return closedDate;
	}

	public void setClosedDate(ZonedDateTime closedDate) {
		this.closedDate = closedDate;
	}

	public List<User> getProgressUser() {
		return progressUser;
	}

	public void setProgressUser(List<User> progressUser) {
		this.progressUser = progressUser;
	}

	public User getClosingUser() {
		return closingUser;
	}

	public void setClosingUser(User closingUser) {
		this.closingUser = closingUser;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(attachment1);
		result = prime * result + Arrays.hashCode(attachment2);
		result = prime * result + Arrays.hashCode(attachment3);
		result = prime * result + ((closedDate == null) ? 0 : closedDate.hashCode());
		result = prime * result + ((closingUser == null) ? 0 : closingUser.hashCode());
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((emailsFwd == null) ? 0 : emailsFwd.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((point == null) ? 0 : point.hashCode());
		result = prime * result + ((progressUser == null) ? 0 : progressUser.hashCode());
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
		Enquiry other = (Enquiry) obj;
		if (!Arrays.equals(attachment1, other.attachment1))
			return false;
		if (!Arrays.equals(attachment2, other.attachment2))
			return false;
		if (!Arrays.equals(attachment3, other.attachment3))
			return false;
		if (closedDate == null) {
			if (other.closedDate != null)
				return false;
		} else if (!closedDate.equals(other.closedDate))
			return false;
		if (closingUser == null) {
			if (other.closingUser != null)
				return false;
		} else if (!closingUser.equals(other.closingUser))
			return false;
		if (created == null) {
			if (other.created != null)
				return false;
		} else if (!created.equals(other.created))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (emailsFwd == null) {
			if (other.emailsFwd != null)
				return false;
		} else if (!emailsFwd.equals(other.emailsFwd))
			return false;
		if (id != other.id)
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (point == null) {
			if (other.point != null)
				return false;
		} else if (!point.equals(other.point))
			return false;
		if (progressUser == null) {
			if (other.progressUser != null)
				return false;
		} else if (!progressUser.equals(other.progressUser))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Enquiry [id=" + id + ", name=" + name + ", email=" + email + ", message=" + message + ", point=" + point
				+ ", emailsFwd=" + emailsFwd + ", attachment1=" + Arrays.toString(attachment1) + ", attachment2="
				+ Arrays.toString(attachment2) + ", attachment3=" + Arrays.toString(attachment3) + ", created="
				+ created + ", closedDate=" + closedDate + ", progressUser=" + progressUser + ", closingUser="
				+ closingUser + "]";
	}

	
		
}
