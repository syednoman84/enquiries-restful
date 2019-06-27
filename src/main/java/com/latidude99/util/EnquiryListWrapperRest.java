package com.latidude99.util;

import com.latidude99.model.Enquiry;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EnquiryListWrapperRest extends EnquiryListWrapper{

    private Long waiting;
    private Long opened;
    private Long closed;

    private Long closedByUser;
    private Long assignedToUser;


    public Long getWaiting() {
        return waiting;
    }

    public void setWaiting(Long waiting) {
        this.waiting = waiting;
    }

    public Long getOpened() {
        return opened;
    }

    public void setOpened(Long opened) {
        this.opened = opened;
    }

    public Long getClosed() {
        return closed;
    }

    public void setClosed(Long closed) {
        this.closed = closed;
    }

    public Long getClosedByUser() {
        return closedByUser;
    }

    public void setClosedByUser(Long closedByUser) {
        this.closedByUser = closedByUser;
    }

    public Long getAssignedToUser() {
        return assignedToUser;
    }

    public void setAssignedToUser(Long assignedToUser) {
        this.assignedToUser = assignedToUser;
    }
}