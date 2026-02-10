package com.safetynet.alerts.service;

import com.safetynet.alerts.dto.PersonInfoDTO;
import com.safetynet.alerts.model.MedicalRecord;
import com.safetynet.alerts.model.Person;
import com.safetynet.alerts.repository.DataRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class PersonInfoService {

    private final DataRepository repo;

    public PersonInfoService(DataRepository repo) {
        this.repo = repo;
    }

    public List<PersonInfoDTO> getPersonInfoByLastName(String lastName) {
        String key = (lastName == null) ? "" : lastName.toLowerCase(Locale.ROOT);

        // Find persons by last name (case-insensitive)
        List<Person> persons = repo.getPersons().stream()
                .filter(p -> p.getLastName() != null
                        && p.getLastName().toLowerCase(Locale.ROOT).equals(key))
                .collect(Collectors.toList());

        // Map each person to DTO with medical record enrichment
        return persons.stream()
                .map(p -> {
                    MedicalRecord mr = repo.findMedical(p.getFirstName(), p.getLastName()).orElse(null);

                    PersonInfoDTO dto = new PersonInfoDTO();
                    dto.firstName = p.getFirstName();
                    dto.lastName = p.getLastName();
                    dto.address = p.getAddress();
                    dto.email = p.getEmail();
                    dto.age = (mr != null && mr.getBirthdate() != null)
                            ? computeAge(mr.getBirthdate())
                            : -1; // or 0 if you prefer
                    dto.medications = (mr != null && mr.getMedications() != null) ? mr.getMedications() : List.of();
                    dto.allergies = (mr != null && mr.getAllergies() != null) ? mr.getAllergies() : List.of();

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Assumes your data.json uses "MM/dd/yyyy" (e.g., 03/06/1984).
    private int computeAge(String birthdate) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate dob = LocalDate.parse(birthdate, fmt);
        return Period.between(dob, LocalDate.now()).getYears();
    }
}
