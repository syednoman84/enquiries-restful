package com.latidude99.model;

public enum Role {
	
	DEFAULT("ROLE_USER"), 
	ADMIN("ROLE_ADMIN"),
	APPADMIN("ROLE_APPADMIN"),
	;
	
	private String text;
	
	Role(String t) {
		text = t;
	}
	
	public String getText() {
		return text;
	}

}
