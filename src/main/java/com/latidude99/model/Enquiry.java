/**
 * Copyright (C) 2018-2019  Piotr Czapik.
 *
 * @author Piotr Czapik
 * <p>
 * This file is part of EnquirySystem.
 * EnquirySystem is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * EnquirySystem is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with EnquirySystem.  If not, see <http://www.gnu.org/licenses/>
 * or write to: latidude99@gmail.com
 */

package com.latidude99.model;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.search.annotations.*;
import org.hibernate.search.annotations.Index;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.*;


@Indexed
@Entity
public class Enquiry implements Serializable {
    private static final long serialVersionUID = -4646260725584082081L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /*
     * @Field - Hibernate Search annotation
     */
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    @NotEmpty(message = "{com.latidude99.model.Enquiry.name.NotEmpty}")
    private String name;

    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    @NotEmpty(message = "{com.latidude99.model.Enquiry.email.NotEmpty}")
    @Email(message = "{com.latidude99.model.Enquiry.email.Email}")
    private String email;

    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    @NotEmpty(message = "{com.latidude99.model.Enquiry.message.NotEmpty}")
    @Size(max = 4096, message = "{com.latidude99.model.Enquiry.message.Size}")
    private String message;

    @Field(index = Index.YES,
            analyze = Analyze.YES,
            store = Store.NO)
    private String phone;

    @Field(index = Index.YES,
            analyze = Analyze.YES,
            store = Store.NO)
    private String isbn;

    private ZonedDateTime createdDate;

    private ZonedDateTime closedDate;

    private String status;

    @Size(max = 4096)
    private String polygon;

    @Size(max = 2048)
    private String polygonEncoded;

    @NotEmpty(message = "{com.latidude99.model.Enquiry.type.NotEmpty}")
    private String type;

    @Lob
    private byte[] image;

    // apparently JPA doesn't allow for more than 2 eagerly fetched collections
    @OneToMany(mappedBy = "enquiry",
//            fetch = FetchType.LAZY, // tests fail with this, without workaround
            cascade = {CascadeType.ALL},
            orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE) // this works for tests
    private List<Comment> comments = new ArrayList<>();

    /*
     * Used for displaying in enquiry/enquiry list views only
     */
    @Transient
    private List<User> sortedProgressUsers = new ArrayList<>();

    /*
     * Used for displaying in enquiry/enquiry list views only
     */
    @Transient
    private List<String> sortedProgressUsersWithDate = new ArrayList<>();

    @OneToMany(mappedBy = "enquiry",
 //           fetch = FetchType.LAZY,
            cascade = {CascadeType.ALL},
            orphanRemoval = true)
    @LazyCollection(LazyCollectionOption.FALSE) // this works for tests`
    private List<Attachment> attachments = new ArrayList<>();



    /*
     * Points of interests, customer enquiry form
     * (not implemented, went with polygons for now)
     */
 /*   @OneToMany(mappedBy = "enquiry",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.ALL},
            orphanRemoval = true)
    private List<Point> point = new ArrayList<>();
*/


    /*
     * Easiest way to persist information about users and the time
     * when they dealt with the enquiry
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @MapKeyTemporal(TemporalType.TIMESTAMP)
    private Map<java.util.Date, User> progressUser = new TreeMap<>();

    @ManyToOne
    @JoinColumn(name = "user_closing_id")
    private User closingUser;

    /*
     * Helper method, saves user with date to the Map
     */
    public void addProgressUser(User user) {
        Map<java.util.Date, User> map = getProgressUser();
        Calendar calendar = Calendar.getInstance();
        java.util.Date now = calendar.getTime();
        java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
        if (!map.isEmpty()) {
            Set<java.util.Date> mapKeys = map.keySet();
            java.util.Date mostRecent = Collections.max(mapKeys);
            User userRecent = map.get(mostRecent);
            if (!userRecent.equals(user)) {
                map.put(currentTimestamp, user);
            }
        } else {
            map.put(currentTimestamp, user);
        }
        setProgressUser(map);
    }

    /*
     * Helper method, removes user with date from the Map
     */
    public void removeProgressUser(User user) {
        Map<java.util.Date, User> map = getProgressUser();
        if (!map.isEmpty()) {
            Set<java.util.Date> mapKeys = map.keySet();
            java.util.Date mostRecent = Collections.max(mapKeys);
            User userRecent = map.get(mostRecent);
            if (userRecent.equals(user)) map.remove(mostRecent);
        }
        setProgressUser(map);
    }

    /*
     * Helper method
     */
    public void addAttachment(Attachment attachment) {
        attachment.setEnquiry(this);
        getAttachments().add(attachment);
    }

    /*
     * Helper method
     */
    public void addComment(Comment comment) {
        comment.setEnquiry(this);
        getComments().add(comment);
    }



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
/*
    public List<Point> getPoint() { return point;  }

    public void setPoint(List<Point> point) {
        this.point = point;
    }
*/
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
//        result = prime * result + ((point == null) ? 0 : point.hashCode());
        result = prime * result + ((progressUser == null) ? 0 : progressUser.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Enquiry other = (Enquiry) obj;
        if (closedDate == null) {
            if (other.closedDate != null) return false;
        } else if (!closedDate.equals(other.closedDate)) return false;
        if (closingUser == null) {
            if (other.closingUser != null) return false;
        } else if (!closingUser.equals(other.closingUser)) return false;
        if (createdDate == null) {
            if (other.createdDate != null) return false;
        } else if (!createdDate.equals(other.createdDate)) return false;
        if (email == null) {
            if (other.email != null) return false;
        } else if (!email.equals(other.email)) return false;
        if (id != other.id) return false;
        if (message == null) {
            if (other.message != null) return false;
        } else if (!message.equals(other.message)) return false;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
/*        if (point == null) {
            if (other.point != null) return false;
        } else if (!point.equals(other.point)) return false;
*/        if (progressUser == null) {
            if (other.progressUser != null) return false;
        } else if (!progressUser.equals(other.progressUser)) return false;
        if (status == null) {
            if (other.status != null) return false;
        } else if (!status.equals(other.status)) return false;
        if (type == null) {
            if (other.type != null) return false;
        } else if (!type.equals(other.type)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "Enquiry [id=" + id +
                ", name=" + name +
                ", email=" + email +
                ", message=" + message +
                ", createdDate=" + createdDate +
                ", closedDate=" + closedDate +
                ", status=" + status +
                ", type=" + type +
//                ", point=" + point +
                ", progressUser=" + progressUser +
                ", closingUser=" + closingUser + "]";
    }


}
