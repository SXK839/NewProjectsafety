package com.safetynet.alerts.service;

import com.safetynet.alerts.model.Firestation;
import com.safetynet.alerts.model.MedicalRecord;
import com.safetynet.alerts.model.Person;
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

    // PERSON

    public void addPerson(Person p) throws IOException {
        repo.addPerson(p);
    }

    public boolean updatePerson(Person p) throws IOException {
        return repo.updatePerson(p);
    }

    public boolean deletePerson(String firstName, String lastName) throws IOException {
        return repo.deletePerson(firstName, lastName);
    }

    // FIRESTATION

    /**
     * Translate repository IOException into a runtime exception, as required by tests.
     */
    public void addFirestation(Firestation f) {
        try {
            repo.addFirestation(f);
        } catch (IOException e) {
            // The test accepts RuntimeException / IllegalStateException / IllegalArgumentException
            throw new IllegalStateException("Failed to add firestation", e);
            // Alternatively:
            // throw new java.io.UncheckedIOException("Failed to add firestation", e);
        }
    }

    public boolean updateFirestation(Firestation f) throws IOException {
        return repo.updateFirestation(f);
    }

    public boolean deleteFirestation(String address) throws IOException {
        return repo.deleteFirestation(address);
    }

    public int deleteFirestationByStation(int station) throws IOException {
        return repo.deleteFirestationByStation(station);
    }

    public Optional<Firestation> getFirestationByAddress(String address) {
        return repo.findStationByAddress(address);
    }

    public List<Firestation> getAllFirestations() {
        return repo.getFirestations();
    }

    // MEDICAL RECORD

    public void addMedicalRecord(MedicalRecord m) throws IOException {
        repo.addMedicalRecord(m);
    }

    public boolean updateMedicalRecord(MedicalRecord m) throws IOException {
        return repo.updateMedicalRecord(m);
    }

    public boolean deleteMedicalRecord(String firstName, String lastName) throws IOException {
        return repo.deleteMedicalRecord(firstName, lastName);
    }

    public Optional<MedicalRecord> getMedicalRecord(String firstName, String lastName) {
        return repo.findMedical(firstName, lastName);
    }

    public List<MedicalRecord> getAllMedicalRecords() {
        return repo.getMedicalrecords();
    }
}