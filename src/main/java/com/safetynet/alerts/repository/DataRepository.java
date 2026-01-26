
package com.safetynet.alerts.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safetynet.alerts.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import java.io.*;
import java.util.*;

@Repository
public class DataRepository {
	private final ObjectMapper mapper = new ObjectMapper();
	@Value("${safetynet.data-file}")
	private Resource dataResource;
	@Value("${safetynet.seed-file}")
	private Resource seedResource;
	private List<Person> persons = new ArrayList<>();
	private List<FirestationMapping> firestations = new ArrayList<>();
	private List<MedicalRecord> medicalrecords = new ArrayList<>();

	public synchronized void load() throws IOException {
		File targetFile;
		try {
			targetFile = dataResource.getFile();
		} catch (IOException e) {
			targetFile = new File("runtime-data/data.json");
		}
		if (!targetFile.exists()) {
			targetFile.getParentFile().mkdirs();
			try (InputStream seedIs = seedResource.getInputStream();
					OutputStream os = new FileOutputStream(targetFile)) {
				seedIs.transferTo(os);
			}
		}
		try (InputStream is = new FileInputStream(targetFile)) {
			Map<String, Object> root = mapper.readValue(is, new TypeReference<Map<String, Object>>() {
			});
			persons = mapper.convertValue(root.get("persons"), new TypeReference<List<Person>>() {
			});
			firestations = mapper.convertValue(root.get("firestations"), new TypeReference<List<FirestationMapping>>() {
			});
			medicalrecords = mapper.convertValue(root.get("medicalrecords"), new TypeReference<List<MedicalRecord>>() {
			});
		}
	}

	public synchronized void save() throws IOException {
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("persons", persons);
		root.put("firestations", firestations);
		root.put("medicalrecords", medicalrecords);
		File targetFile;
		try {
			targetFile = dataResource.getFile();
		} catch (IOException e) {
			targetFile = new File("runtime-data/data.json");
		}
		targetFile.getParentFile().mkdirs();
		mapper.writerWithDefaultPrettyPrinter().writeValue(targetFile, root);
	}

	public List<Person> getPersons() {
		return persons;
	}

	public List<FirestationMapping> getFirestations() {
		return firestations;
	}

	public List<MedicalRecord> getMedicalrecords() {
		return medicalrecords;
	}

	public Optional<MedicalRecord> findMedical(String f, String l) {
		return medicalrecords.stream()
				.filter(m -> m.getFirstName().equalsIgnoreCase(f) && m.getLastName().equalsIgnoreCase(l)).findFirst();
	}

	public Optional<FirestationMapping> findStationByAddress(String a) {
		return firestations.stream().filter(f -> f.getAddress().equalsIgnoreCase(a)).findFirst();
	}

	public List<Person> findPersonsByAddress(String a) {
		List<Person> res = new ArrayList<>();
		for (Person p : persons)
			if (p.getAddress().equalsIgnoreCase(a))
				res.add(p);
		return res;
	}

	public List<Person> findPersonsByStation(int s) {
		Set<String> addrs = new HashSet<>();
		for (FirestationMapping f : firestations)
			if (f.getStation() == s)
				addrs.add(f.getAddress());
		List<Person> res = new ArrayList<>();
		for (Person p : persons)
			if (addrs.contains(p.getAddress()))
				res.add(p);
		return res;
	}

	public synchronized void addPerson(Person p) throws IOException {
		persons.add(p);
		save();
	}

	public synchronized boolean updatePerson(Person p) throws IOException {
		for (int i = 0; i < persons.size(); i++) {
			Person cur = persons.get(i);
			if (cur.getFirstName().equalsIgnoreCase(p.getFirstName())
					&& cur.getLastName().equalsIgnoreCase(p.getLastName())) {
				p.setFirstName(cur.getFirstName());
				p.setLastName(cur.getLastName());
				persons.set(i, p);
				save();
				return true;
			}
		}
		return false;
	}

	public synchronized boolean deletePerson(String f, String l) throws IOException {
		boolean removed = persons
				.removeIf(x -> x.getFirstName().equalsIgnoreCase(f) && x.getLastName().equalsIgnoreCase(l));
		if (removed)
			save();
		return removed;
	}

	public synchronized void addFirestation(FirestationMapping fm) throws IOException {
		firestations.add(fm);
		save();
	}

	public synchronized boolean updateFirestation(FirestationMapping fm) throws IOException {
		for (int i = 0; i < firestations.size(); i++) {
			FirestationMapping cur = firestations.get(i);
			if (cur.getAddress().equalsIgnoreCase(fm.getAddress())) {
				firestations.set(i, fm);
				save();
				return true;
			}
		}
		return false;
	}

	public synchronized boolean deleteFirestation(String addressOrStation) throws IOException {
		boolean changed = false;
		try {
			int s = Integer.parseInt(addressOrStation);
			changed = firestations.removeIf(x -> x.getStation() == s);
		} catch (NumberFormatException e) {
			changed = firestations.removeIf(x -> x.getAddress().equalsIgnoreCase(addressOrStation));
		}
		if (changed)
			save();
		return changed;
	}

	public synchronized void addMedicalRecord(MedicalRecord m) throws IOException {
		medicalrecords.add(m);
		save();
	}

	public synchronized boolean updateMedicalRecord(MedicalRecord m) throws IOException {
		for (int i = 0; i < medicalrecords.size(); i++) {
			MedicalRecord cur = medicalrecords.get(i);
			if (cur.getFirstName().equalsIgnoreCase(m.getFirstName())
					&& cur.getLastName().equalsIgnoreCase(m.getLastName())) {
				medicalrecords.set(i, m);
				save();
				return true;
			}
		}
		return false;
	}

	public synchronized boolean deleteMedicalRecord(String f, String l) throws IOException {
		boolean removed = medicalrecords
				.removeIf(x -> x.getFirstName().equalsIgnoreCase(f) && x.getLastName().equalsIgnoreCase(l));
		if (removed)
			save();
		return removed;
	}
}
