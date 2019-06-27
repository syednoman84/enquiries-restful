package com.latidude99.util;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.latidude99.model.Attachment;
import com.latidude99.model.Enquiry;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AttachmentStatsRest {

    private long id;

    private String name;

    private String mimetype;

    private long size;

    private long enquiryId;

    public AttachmentStatsRest() {
    }

    public AttachmentStatsRest(Enquiry enquiry, Attachment attachment) {
        this.id = attachment.getId();
        this.name = attachment.getName();
        this.mimetype = attachment.getMimetype();
        this.size = attachment.getSize();
        this.enquiryId = enquiry.getId();
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


    public long getEnquiryId() {
        return enquiryId;
    }

    public void setEnquiryId(long enquiryId) {
        this.enquiryId = enquiryId;
    }




}