package com.lice.bean;

import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * description: Student <br>
 * date: 2019/8/17 10:58 <br>
 * author: lc <br>
 * version: 1.0 <br>
 */
@Component
public class Student {

	private String stuId;
	private String stuName;
	private String address;
	private Date birthday;

	public String getStuId() {
		return stuId;
	}

	public void setStuId(String stuId) {
		this.stuId = stuId;
	}

	public String getStuName() {
		return stuName;
	}

	public void setStuName(String stuName) {
		this.stuName = stuName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	@Override
	public String toString() {
		return "Student{" +
				"stuId='" + stuId + '\'' +
				", stuName='" + stuName + '\'' +
				", address='" + address + '\'' +
				", birthday=" + birthday +
				'}';
	}
}
