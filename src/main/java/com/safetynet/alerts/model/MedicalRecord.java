
package com.safetynet.alerts.model;

import java.util.List;

public class MedicalRecord {
	private String firstName;
	private String lastName;
	private String birthdate;
	private List<String> medications;
	private List<String> allergies;

	public MedicalRecord() {
	}

	public MedicalRecord(String f, String l, String b, List<String> m, List<String> a) {
		firstName = f;
		lastName = l;
		birthdate = b;
		medications = m;
		allergies = a;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String v) {
		firstName = v;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String v) {
		lastName = v;
	}

	public String getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(String v) {
		birthdate = v;
	}

	public List<String> getMedications() {
		return medications;
	}

	public void setMedications(List<String> v) {
		medications = v;
	}

	public List<String> getAllergies() {
		return allergies;
	}

	public void setAllergies(List<String> v) {
		allergies = v;
	}
}
