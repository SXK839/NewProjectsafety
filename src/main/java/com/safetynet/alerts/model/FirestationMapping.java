
package com.safetynet.alerts.model;

public class FirestationMapping {
	private String address;
	private int station;

	public FirestationMapping() {
	}

	public FirestationMapping(String a, int s) {
		address = a;
		station = s;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String a) {
		address = a;
	}

	public int getStation() {
		return station;
	}

	public void setStation(int s) {
		station = s;
	}
}
