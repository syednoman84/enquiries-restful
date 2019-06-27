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

package com.latidude99.util;

import java.util.ArrayList;
import java.util.List;

/*
 * Transfers search parameters in the search section  form
 * on the main enquiry list page
 */

public class SearchWrapper {


    private String searchFor;
    private String selector;
    private int limit;
    private String searchIn;
    private String status;
    private String dateRange;
    private String sortBy;
    private String direction;
    private List<String> userList;
    private String assignedUser;
    private String closingUser;
  //  private String customer;

    public SearchWrapper(){
        searchFor = "";
        selector = "";
        limit = 0;
        searchIn = "";
        status = "all";
        dateRange = "";
        sortBy = "";
        direction = "";
        userList = new ArrayList<>();
        assignedUser = "any user";
        closingUser = "any user";
    }

    public String getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(String assignedUser) {
        this.assignedUser = assignedUser;
    }

    public String getClosingUser() {
        return closingUser;
    }

    public void setClosingUser(String closingUser) {
        this.closingUser = closingUser;
    }

    public String getSearchFor() {
        return searchFor;
    }

    public void setSearchFor(String searchFor) {
        this.searchFor = searchFor;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getSearchIn() {
        return searchIn;
    }

    public void setSearchIn(String searchIn) {
        this.searchIn = searchIn;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public List<String> getUserList() {
        return userList;
    }

    public void setUserList(List<String> userList) {
        this.userList = userList;
    }
/*

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }
*/

    @Override
    public String toString() {
        return "SearchWrapper{" + "searchFor='" + searchFor + '\'' + ", selector='" + selector + '\'' + ", limit=" + limit + ", searchIn='" + searchIn + '\'' + ", status='" + status + '\'' + ", dateRange='" + dateRange + '\'' + ", sortBy='" + sortBy + '\'' + ", direction='" + direction + '\'' + ", userList=" + userList + ", assignedUser='" + assignedUser + '\'' + ", closingUser='" + closingUser + '\'' + '}';
    }
}





























