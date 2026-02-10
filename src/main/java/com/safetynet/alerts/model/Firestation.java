
package com.safetynet.alerts.model;

public class Firestation {
	private String address;
	private int station;

	public Firestation() {
	}

	public Firestation(String a, int s) {
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
