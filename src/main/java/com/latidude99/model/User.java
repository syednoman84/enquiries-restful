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

import java.io.Serializable;
import java.sql.Date;
import java.time.Instant;
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
import javax.persistence.Transient;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;


@Entity
public class User implements Serializable {
    private static final long serialVersionUID = 8404628707234409704L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotEmpty(message = "{com.latidude99.model.User.name.NotEmpty}")
    private String name;

    @NotEmpty(message = "{com.latidude99.model.User.email.NotEmpty}")
    @Email(message = "{com.latidude99.model.User.email.Email}")
    private String email;

    @Size(min = 6, message = "{com.latidude99.model.User.password.Size}")
    private String password;

    // used only when resetting passwords, no need to store that
    @Transient
    @Size(min = 6, message = "{com.latidude99.model.User.password.Size}")
    private String passwordNew;

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private boolean enabled;

    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private boolean blocked;

    private String activationToken;

    private String resetToken;

    private ZonedDateTime registered;

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private Set<UserRole> roles = new HashSet<>();

    // enquiries that user has been dealing with
    @ManyToMany(mappedBy = "progressUser", cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    private List<Enquiry> enquiriesProgress = new ArrayList<>();

    // enquiries that user has closed
    @OneToMany(mappedBy = "closingUser", fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<Enquiry> enquiriesClosed = new ArrayList<>();


    // when user added
    @PrePersist
    protected void onCreate() {
        registered = ZonedDateTime.now();
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getResetToken() {
        return resetToken;
    }


    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public boolean isBlocked() {
        return blocked;
    }


    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }


    public String getPasswordNew() {
        return passwordNew;
    }


    public void setPasswordNew(String passwordNew) {
        this.passwordNew = passwordNew;
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


    public String getActivationToken() {
        return activationToken;
    }


    public void setActivationToken(String activationToken) {
        this.activationToken = activationToken;
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
        result = prime * result + ((activationToken == null) ? 0 : activationToken.hashCode());
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + (enabled ? 1231 : 1237);
        result = prime * result + ((enquiriesClosed == null) ? 0 : enquiriesClosed.hashCode());
        result = prime * result + ((enquiriesProgress == null) ? 0 : enquiriesProgress.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((registered == null) ? 0 : registered.hashCode());
        result = prime * result + ((roles == null) ? 0 : roles.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        User other = (User) obj;
        if (activationToken == null) {
            if (other.activationToken != null) return false;
        } else if (!activationToken.equals(other.activationToken)) return false;
        if (email == null) {
            if (other.email != null) return false;
        } else if (!email.equals(other.email)) return false;
        if (enabled != other.enabled) return false;
        if (enquiriesClosed == null) {
            if (other.enquiriesClosed != null) return false;
        } else if (!enquiriesClosed.equals(other.enquiriesClosed)) return false;
        if (enquiriesProgress == null) {
            if (other.enquiriesProgress != null) return false;
        } else if (!enquiriesProgress.equals(other.enquiriesProgress)) return false;
        if (id != other.id) return false;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        if (password == null) {
            if (other.password != null) return false;
        } else if (!password.equals(other.password)) return false;
        if (registered == null) {
            if (other.registered != null) return false;
        } else if (!registered.equals(other.registered)) return false;
        if (roles == null) {
            if (other.roles != null) return false;
        } else if (!roles.equals(other.roles)) return false;
        return true;
    }


    @Override
    public String toString() {
        return "User [id=" + id + ", name=" + name + ", email=" + email +
                ", password=" + password + ", enabled=" + enabled +
                ", activationToken=" + activationToken +
                ", registered=" + registered + ", roles=" + roles +
                ", enquiriesProgress=" + enquiriesProgress.size() +
                ", enquiriesClosed=" + enquiriesClosed.size() + "]";
    }


}




























