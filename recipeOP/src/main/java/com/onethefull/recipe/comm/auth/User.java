package com.onethefull.recipe.comm.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class User extends UserSimple {
	
	private static final long serialVersionUID = 482679958205176642L;


	private String password;
	private String authToken;
	private String email;
	private String mobile;
	
	public User() {
		super();
	}	
	
	public User(String id, String authToken) {
		super();
		this.setId(id);
		this.setAuthToken(authToken);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	@JsonIgnore
	public UserSimple getUserSimple() {
		return (UserSimple) this;
	}
}
