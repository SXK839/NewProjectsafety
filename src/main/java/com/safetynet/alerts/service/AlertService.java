
package com.safetynet.alerts.service;

import com.safetynet.alerts.dto.*;
import com.safetynet.alerts.model.*;
import com.safetynet.alerts.repository.DataRepository;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlertService {
	private final DataRepository repo;
	private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");

	public AlertService(DataRepository repo) {
		this.repo = repo;
	}

	private int ageFromBirthdate(String b) {
		try {
			LocalDate dob = LocalDate.parse(b, fmt);
			return Period.between(dob, LocalDate.now()).getYears();
		} catch (Exception e) {
			return -1;
		}
	}

	public Map<String, Object> firestation(int s) {
		List<Person> persons = repo.findPersonsByStation(s);
		if (persons.isEmpty())
			return new HashMap<>();
		List<PersonSummaryDTO> list = new ArrayList<>();
		int adults = 0, children = 0;
		for (Person p : persons) {
			PersonSummaryDTO dto = new PersonSummaryDTO();
			dto.firstName = p.getFirstName();
			dto.lastName = p.getLastName();
			dto.address = p.getAddress();
			dto.phone = p.getPhone();
			list.add(dto);
			Optional<MedicalRecord> mr = repo.findMedical(p.getFirstName(), p.getLastName());
			int age = mr.map(m -> ageFromBirthdate(m.getBirthdate())).orElse(-1);
			if (age >= 0 && age <= 18)
				children++;
			else if (age > 18)
				adults++;
		}
		Map<String, Object> res = new LinkedHashMap<>();
		res.put("persons", list);
		res.put("adults", adults);
		res.put("children", children);
		return res;
	}

	public Object childAlert(String address) {
		List<Person> ppl = repo.findPersonsByAddress(address);
		List<ChildAlertDTO> children = new ArrayList<>();
		List<String> household = ppl.stream().map(p -> p.getFirstName() + " " + p.getLastName())
				.collect(Collectors.toList());
		for (Person p : ppl) {
			Optional<MedicalRecord> mr = repo.findMedical(p.getFirstName(), p.getLastName());
			int age = mr.map(m -> ageFromBirthdate(m.getBirthdate())).orElse(-1);
			if (age >= 0 && age <= 18) {
				ChildAlertDTO c = new ChildAlertDTO();
				c.firstName = p.getFirstName();
				c.lastName = p.getLastName();
				c.age = age;
				c.otherHouseholdMembers = household.stream()
						.filter(h -> !h.equals(p.getFirstName() + " " + p.getLastName())).collect(Collectors.toList());
				children.add(c);
			}
		}
		if (children.isEmpty())
			return new HashMap<>();
		return children;
	}

	public Object phoneAlert(int s) {
		List<Person> persons = repo.findPersonsByStation(s);
		if (persons.isEmpty())
			return new HashMap<>();
		Set<String> phones = persons.stream().map(Person::getPhone)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		Map<String, Object> res = new LinkedHashMap<>();
		res.put("phones", phones);
		return res;
	}

	public Object fire(String address) {
		List<Person> ppl = repo.findPersonsByAddress(address);
		if (ppl.isEmpty())
			return new HashMap<>();
		int station = repo.findStationByAddress(address).map(FirestationMapping::getStation).orElse(-1);
		List<ResidentDetailsDTO> residents = new ArrayList<>();
		for (Person p : ppl) {
			ResidentDetailsDTO r = new ResidentDetailsDTO();
			r.firstName = p.getFirstName();
			r.lastName = p.getLastName();
			r.phone = p.getPhone();
			Optional<MedicalRecord> mr = repo.findMedical(p.getFirstName(), p.getLastName());
			r.age = mr.map(m -> ageFromBirthdate(m.getBirthdate())).orElse(-1);
			r.medications = mr.map(MedicalRecord::getMedications).orElse(Collections.emptyList());
			r.allergies = mr.map(MedicalRecord::getAllergies).orElse(Collections.emptyList());
			residents.add(r);
		}
		FireResponseDTO dto = new FireResponseDTO();
		dto.station = station;
		dto.residents = residents;
		return dto;
	}

	public Object floodStations(List<Integer> stations) {
		Set<String> addrs = repo.getFirestations().stream().filter(f -> stations.contains(f.getStation()))
				.map(FirestationMapping::getAddress).collect(Collectors.toSet());
		if (addrs.isEmpty())
			return new HashMap<>();
		Map<String, List<ResidentDetailsDTO>> out = new LinkedHashMap<>();
		for (String addr : addrs) {
			List<Person> ppl = repo.findPersonsByAddress(addr);
			List<ResidentDetailsDTO> residents = new ArrayList<>();
			for (Person p : ppl) {
				ResidentDetailsDTO r = new ResidentDetailsDTO();
				r.firstName = p.getFirstName();
				r.lastName = p.getLastName();
				r.phone = p.getPhone();
				Optional<MedicalRecord> mr = repo.findMedical(p.getFirstName(), p.getLastName());
				r.age = mr.map(m -> ageFromBirthdate(m.getBirthdate())).orElse(-1);
				r.medications = mr.map(MedicalRecord::getMedications).orElse(Collections.emptyList());
				r.allergies = mr.map(MedicalRecord::getAllergies).orElse(Collections.emptyList());
				residents.add(r);
			}
			out.put(addr, residents);
		}
		return out;
	}

	public Object personInfo(String lastName) {
		List<Person> persons = repo.getPersons().stream().filter(p -> p.getLastName().equalsIgnoreCase(lastName))
				.collect(Collectors.toList());
		if (persons.isEmpty())
			return new HashMap<>();
		List<PersonInfoDTO> res = new ArrayList<>();
		for (Person p : persons) {
			Optional<MedicalRecord> mr = repo.findMedical(p.getFirstName(), p.getLastName());
			PersonInfoDTO dto = new PersonInfoDTO();
			dto.firstName = p.getFirstName();
			dto.lastName = p.getLastName();
			dto.address = p.getAddress();
			dto.email = p.getEmail();
			dto.age = mr.map(m -> ageFromBirthdate(m.getBirthdate())).orElse(-1);
			dto.medications = mr.map(MedicalRecord::getMedications).orElse(Collections.emptyList());
			dto.allergies = mr.map(MedicalRecord::getAllergies).orElse(Collections.emptyList());
			res.add(dto);
		}
		return res;
	}

	public Object communityEmail(String city) {
		List<String> emails = repo.getPersons().stream().filter(p -> p.getCity().equalsIgnoreCase(city))
				.map(Person::getEmail).distinct().collect(Collectors.toList());
		if (emails.isEmpty())
			return new HashMap<>();
		CommunityEmailDTO dto = new CommunityEmailDTO();
		dto.emails = emails;
		return dto;
	}
}
