
package com.safetynet.alerts.service;

import com.safetynet.alerts.model.*;
import com.safetynet.alerts.repository.DataRepository;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {
	private final DataRepository repo;

	public AdminService(DataRepository repo) {
		this.repo = repo;
	}

	public void addPerson(Person p) throws IOException {
		repo.addPerson(p);
	}

	public boolean updatePerson(Person p) throws IOException {
		return repo.updatePerson(p);
	}

	public boolean deletePerson(String f, String l) throws IOException {
		return repo.deletePerson(f, l);
	}

	public void addFirestation(Firestation f) throws IOException {
		repo.addFirestation(f);
	}

	public boolean updateFirestation(Firestation f) throws IOException {
		return repo.updateFirestation(f);
	}

	public boolean deleteFirestation(String a) throws IOException {
		return repo.deleteFirestation(a);
	}

	public void addMedicalRecord(MedicalRecord m) throws IOException {
		repo.addMedicalRecord(m);
	}

	public boolean updateMedicalRecord(MedicalRecord m) throws IOException {
		return repo.updateMedicalRecord(m);
	}

	public boolean deleteMedicalRecord(String f, String l) throws IOException {
		return repo.deleteMedicalRecord(f, l);
	}

	// Get one mapping by address
	public java.util.Optional<com.safetynet.alerts.model.Firestation> getFirestationByAddress(String address) {
	    return repo.findStationByAddress(address);
	}

	// Get all mappings
	public java.util.List<com.safetynet.alerts.model.Firestation> getAllFirestations() {
	    return repo.getFirestations();
	}
	
    public int deleteFirestationByStation(int station) throws IOException {
    return repo.deleteFirestationByStation(station);
}
    
 // Add these imports if missing:
 // import java.util.Optional;
 // import java.util.List;

 public Optional<MedicalRecord> getMedicalRecord(String firstName, String lastName) {
     return repo.findMedical(firstName, lastName);
 }

 public List<MedicalRecord> getAllMedicalRecords() {
     return repo.getMedicalrecords();
 }


}
