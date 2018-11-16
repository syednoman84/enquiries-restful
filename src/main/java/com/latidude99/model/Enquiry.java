package com.latidude99.model;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.Basic;
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
import javax.persistence.MapKeyTemporal;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;


@Indexed
@Entity
public class Enquiry implements Serializable{
	private static final long serialVersionUID = -4646260725584082081L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
	@NotEmpty(message = "{com.latidude99.model.Enquiry.name.NotEmpty}")
	private String name;
	
	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
	@NotEmpty(message = "{com.latidude99.model.Enquiry.email.NotEmpty}")
	@Email(message = "{com.latidude99.model.Enquiry.email.Email}")
	private String email;
	
	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
	@NotEmpty(message = "{com.latidude99.model.Enquiry.message.NotEmpty}")
	@Size(max=4096, message = "{com.latidude99.model.Enquiry.message.Size}")
	private String message;
	
	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
	private String phone;
	
	@Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
	private String isbn;
	
	private ZonedDateTime createdDate;
	
	private ZonedDateTime closedDate;
	
	private String status;
	
	@Size(max=4096)
	private String polygon;
	
	@Size(max=2048)
	private String polygonEncoded;
	
	@NotEmpty(message = "{com.latidude99.model.Enquiry.type.NotEmpty}")
	private String type;
	
	@Lob
	private byte[] image;
	
	@OneToMany(mappedBy = "enquiry",
			fetch = FetchType.LAZY, 
			cascade = {CascadeType.ALL }, 
			orphanRemoval = true)
	private List<Comment> comments = new ArrayList<>();
	
	@Transient //formatting for the View
	private List<User> sortedProgressUsers = new ArrayList<>();
	
	@Transient //formatting for the View
	private List<String> sortedProgressUsersWithDate = new ArrayList<>();
	
	@OneToMany(mappedBy = "enquiry",
			fetch = FetchType.LAZY, 
			cascade = {CascadeType.ALL }, 
			orphanRemoval = true)
	private List<Attachment> attachments = new ArrayList<>();
		
	@OneToMany(mappedBy = "enquiry", 
			fetch = FetchType.LAZY, 
			cascade = {CascadeType.ALL }, 
			orphanRemoval = true)
	private List<Point> point = new ArrayList<>();
	
	@ManyToMany(
			fetch = FetchType.EAGER,
			cascade = CascadeType.PERSIST)
	@MapKeyTemporal(TemporalType.TIMESTAMP)
    private Map<java.util.Date, User> progressUser = new TreeMap<>();
	
	@ManyToOne
    @JoinColumn(name = "user_closing_id")
    private User closingUser;
	
		
	
/*	
	@PrePersist
	protected void onCreate() {
	    createdDate = ZonedDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {

	}
*/
	//to save in DB
	public void addProgressUser(User user) {
		Map<java.util.Date, User> map = getProgressUser();
		Calendar calendar = Calendar.getInstance();
		java.util.Date now = calendar.getTime();
		java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
		if(!map.isEmpty()) {
			Set<java.util.Date> mapKeys = map.keySet();
			java.util.Date mostRecent = Collections.max(mapKeys);
			User userRecent = map.get(mostRecent);
			if(!userRecent.equals(user)) {
				map.put(currentTimestamp, user);
			}//else if(mostRecent != currentTimestamp)
			//	map.put(currentTimestamp, user);
		}else {
			map.put(currentTimestamp, user);
		}
		setProgressUser(map);
	}
	
	public void removeProgressUser(User user) {
		Map<java.util.Date, User> map = getProgressUser();
		if(!map.isEmpty()) {
			Set<java.util.Date> mapKeys = map.keySet();
			java.util.Date mostRecent = Collections.max(mapKeys);
			User userRecent = map.get(mostRecent);
			if(userRecent.equals(user))
				map.remove(mostRecent);
		}
		setProgressUser(map);
	}

		
	//to save in DB
	public void addAttachment(Attachment attachment) {
		attachment.setEnquiry(this);
		getAttachments().add(attachment);
	}
	
	public void addComment(Comment comment) {
		comment.setEnquiry(this);
		getComments().add(comment);
	}
	
	
//-------------------------------------------------	
		
	

	
	
	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public String getPolygon() {
		return polygon;
	}
	
	public String getPolygonEncoded() {
		return polygonEncoded;
	}

	public void setPolygonEncoded(String polygonEncoded) {
		this.polygonEncoded = polygonEncoded;
	}

	public void setPolygon(String polygon) {
		this.polygon = polygon;
	}

	public String getPhone() {
		return phone;
	}
	
	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	
	public List<Attachment> getAttachments() {
		return attachments;
	}
	
	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}
	
	public List<User> getSortedProgressUsers() {
		return sortedProgressUsers;
	}

	public List<String> getSortedProgressUsersWithDate() {
		return sortedProgressUsersWithDate;
	}

	public void setSortedProgressUsersWithDate(List<String> sortedProgressUsersWithDate) {
		this.sortedProgressUsersWithDate = sortedProgressUsersWithDate;
	}

	public void setSortedProgressUsers(List<User> sortedProgressUsers) {
		this.sortedProgressUsers = sortedProgressUsers;
	}

	
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

	
	public ZonedDateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(ZonedDateTime createdDate) {
		this.createdDate = createdDate;
	}

	public ZonedDateTime getClosedDate() {
		return closedDate;
	}

	public void setClosedDate(ZonedDateTime closedDate) {
		this.closedDate = closedDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Point> getPoint() {
		return point;
	}

	public void setPoint(List<Point> point) {
		this.point = point;
	}

	
	public User getClosingUser() {
		return closingUser;
	}

	public Map<java.util.Date, User> getProgressUser() {
		return progressUser;
	}

	public void setProgressUser(Map<java.util.Date, User> progressUser) {
		this.progressUser = progressUser;
	}

	public void setClosingUser(User closingUser) {
		this.closingUser = closingUser;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((closedDate == null) ? 0 : closedDate.hashCode());
		result = prime * result + ((closingUser == null) ? 0 : closingUser.hashCode());
		result = prime * result + ((createdDate == null) ? 0 : createdDate.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((point == null) ? 0 : point.hashCode());
		result = prime * result + ((progressUser == null) ? 0 : progressUser.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		if (createdDate == null) {
			if (other.createdDate != null)
				return false;
		} else if (!createdDate.equals(other.createdDate))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
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
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Enquiry [id=" + id + ", name=" + name + ", email=" + email + ", message=" + message + ", createdDate="
				+ createdDate + ", closedDate=" + closedDate + ", status=" + status + ", type=" + type + ", point="
				+ point + ", progressUser=" + progressUser + ", closingUser=" + closingUser
				+ "]";
	}

	

		
}
