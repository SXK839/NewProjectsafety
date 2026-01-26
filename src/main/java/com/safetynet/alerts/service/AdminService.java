
package com.safetynet.alerts.service;

import com.safetynet.alerts.model.*;
import com.safetynet.alerts.repository.DataRepository;
import org.springframework.stereotype.Service;
import java.io.IOException;

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

	public void addFirestation(FirestationMapping f) throws IOException {
		repo.addFirestation(f);
	}

	public boolean updateFirestation(FirestationMapping f) throws IOException {
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
}
