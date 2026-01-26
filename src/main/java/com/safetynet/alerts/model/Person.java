
package com.safetynet.alerts.model;

public class Person {
	private String firstName;
	private String lastName;
	private String address;
	private String city;
	private String zip;
	private String phone;
	private String email;

	public Person() {
	}

	public Person(String firstName, String lastName, String address, String city, String zip, String phone,
			String email) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.city = city;
		this.zip = zip;
		this.phone = phone;
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String v) {
		this.firstName = v;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String v) {
		this.lastName = v;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String v) {
		this.address = v;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String v) {
		this.city = v;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String v) {
		this.zip = v;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String v) {
		this.phone = v;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String v) {
		this.email = v;
	}
}
