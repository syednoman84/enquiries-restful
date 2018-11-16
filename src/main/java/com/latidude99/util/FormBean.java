package com.latidude99.util;

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































