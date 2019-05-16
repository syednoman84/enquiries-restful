/**Copyright (C) 2018-2019  Piotr Czapik.
 * @author Piotr Czapik
 *
 *  This file is part of EnquirySystem.
 *  EnquirySystem is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  EnquirySystem is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with EnquirySystem.  If not, see <http://www.gnu.org/licenses/>
 *  or write to: latidude99@gmail.com
 */

package com.latidude99.util;

/*
 * Individual enquiry view, user comments form helper object
 */

public class FormBean {
	
	private String selector;
	private int number;
	private String numbersAsString;
	
	private String enquiryId;
	private String commentTitle;
	private String commentContent;
	private String userId;
	
	
	public FormBean() {}

	
	public String getUserId() {
		return userId;
	}


	public void setUserId(String userId) {
		this.userId = userId;
	}


	public String getEnquiryId() {
		return enquiryId;
	}


	public void setEnquiryId(String enquiryId) {
		this.enquiryId = enquiryId;
	}


	public String getCommentTitle() {
		return commentTitle;
	}


	public void setCommentTitle(String commentTitle) {
		this.commentTitle = commentTitle;
	}


	public String getCommentContent() {
		return commentContent;
	}


	public void setCommentContent(String commentContent) {
		this.commentContent = commentContent;
	}


	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getNumbersAsString() {
		return numbersAsString;
	}

	public void setNumbersAsString(String numbersAsString) {
		this.numbersAsString = numbersAsString;
	}

	
}































