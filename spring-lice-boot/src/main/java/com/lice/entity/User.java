package com.lice.entity;

/**
 * description: User <br>
 * date: 2019/10/15 15:04 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
public class User {

	private String userId;
	private String userName;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public String toString() {
		return "User{" +
				"userId='" + userId + '\'' +
				", userName='" + userName + '\'' +
				'}';
	}
}
