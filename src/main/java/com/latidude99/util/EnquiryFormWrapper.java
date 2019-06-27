package com.latidude99.util;

import com.latidude99.model.Enquiry;
import org.springframework.scheduling.support.SimpleTriggerContext;

import java.util.Map;
import java.util.TreeMap;

public class EnquiryFormWrapper extends Enquiry {

    private Map<String, String> formReturnInfo;
    private int attachmentsNumber;

    public EnquiryFormWrapper(){
        formReturnInfo = new TreeMap<>();
    }

    public EnquiryFormWrapper(Enquiry enquiry){
        this.setName(enquiry.getName());
        this.setEmail(enquiry.getEmail());
        this.setPhone(enquiry.getPhone());
        this.setType(enquiry.getType());
        this.setMessage(enquiry.getMessage());
        this.setId(enquiry.getId());
        this.setIsbn(enquiry.getIsbn());
        this.setPolygon(enquiry.getPolygon());
        this.setPolygonEncoded(enquiry.getPolygonEncoded());
        formReturnInfo = new TreeMap<>();
        this.attachmentsNumber = enquiry.getAttachments().size();
    }

    public Map<String, String> getFormReturnInfo() {
        return formReturnInfo;
    }

    public void setFormReturnInfo(Map<String, String> formReturnInfo) {
        this.formReturnInfo = formReturnInfo;
    }

    public int getAttachmentsNumber() {
        return attachmentsNumber;
    }

    public void setAttachmentsNumber(int attachmentsNumber) {
        this.attachmentsNumber = attachmentsNumber;
    }
}
