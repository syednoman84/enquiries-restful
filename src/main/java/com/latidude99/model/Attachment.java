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

import com.fasterxml.jackson.annotation.JsonBackReference;

import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
public class Attachment implements Serializable {
    private static final long serialVersionUID = 6279369246216631207L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private String mimetype;

    private long size;

    @Lob
    private byte[] file;

    @ManyToOne
    @JoinColumn(name = "enquiry_id")
    @JsonBackReference(value="enquiry-attachments")
    private Enquiry enquiry;

    public Attachment() {
    }

    public Attachment(String name, String mimetype, byte[] file) {
        this.name = name;
        this.mimetype = mimetype;
        this.file = file;
    }

    public Attachment(long id, String name, String mimetype, long size) {
        this.id = id;
        this.name = name;
        this.mimetype = mimetype;
        this.size = size;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public Enquiry getEnquiry() {
        return enquiry;
    }

    public void setEnquiry(Enquiry enquiry) {
        this.enquiry = enquiry;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((enquiry == null) ? 0 : enquiry.hashCode());
        result = prime * result + Arrays.hashCode(file);
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((mimetype == null) ? 0 : mimetype.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Attachment other = (Attachment) obj;
        if (enquiry == null) {
            if (other.enquiry != null) return false;
        } else if (!enquiry.equals(other.enquiry)) return false;
        if (!Arrays.equals(file, other.file)) return false;
        if (id != other.id) return false;
        if (mimetype == null) {
            if (other.mimetype != null) return false;
        } else if (!mimetype.equals(other.mimetype)) return false;
        if (name == null) {
            if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "Attachment [id=" + id +
                ", name=" + name +
                ", mimetype=" + mimetype +
                ", enquiry=" + enquiry + "]";
    }


}
