package com.safetynet.alerts.service;

import com.safetynet.alerts.model.Person;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PersonService {

    // Temporary in-memory store until DB or JSON integration
    private final Map<String, Person> people = new HashMap<>();

    private String key(String first, String last) {
        String f = first == null ? "" : first.toLowerCase();
        String l = last == null ? "" : last.toLowerCase();
        return f + "_" + l;
    }

    /** Add new person; fail if already exists */
    public Person addPerson(Person person) {
        if (person == null || person.getFirstName() == null || person.getLastName() == null) {
            throw new IllegalArgumentException("firstName and lastName are required");
        }
        String k = key(person.getFirstName(), person.getLastName());
        if (people.containsKey(k)) {
            throw new IllegalStateException("Person already exists with given firstName and lastName");
        }
        people.put(k, person);
        return person;
    }

    /** Update existing person (first/last name immutable). Throws if missing. */
    public Person updatePerson(String firstName, String lastName, Person payload) {
        if (firstName == null || lastName == null) {
            throw new IllegalArgumentException("firstName and lastName are required");
        }
        String k = key(firstName, lastName);
        Person existing = people.get(k);
        if (existing == null) {
            throw new IllegalArgumentException("Person not found");
        }

        // Enforce immutable names
        existing.setAddress(payload.getAddress());
        existing.setCity(payload.getCity());
        existing.setZip(payload.getZip());
        existing.setPhone(payload.getPhone());
        existing.setEmail(payload.getEmail());

        // If you prefer replacing the whole object apart from names:
        // Person updated = new Person(existing.getFirstName(), existing.getLastName(),
        //         payload.getAddress(), payload.getCity(), payload.getZip(),
        //         payload.getPhone(), payload.getEmail());
        // people.put(k, updated);
        // return updated;

        return existing;
    }

    public boolean deletePerson(String firstName, String lastName) {
        return people.remove(key(firstName, lastName)) != null;
    }

    public Optional<Person> getPerson(String firstName, String lastName) {
        if (firstName == null || lastName == null) return Optional.empty();
        return Optional.ofNullable(people.get(key(firstName, lastName)));
    }

    public List<Person> getAllPersons() {
        return new ArrayList<>(people.values());
    }

    /*
     * Legacy helper that allowed create-or-update; keep if needed elsewhere,
     * but DO NOT use it for the /person PUT endpoint that must be update-only.
     */
    @Deprecated
    public Person upsertPerson(Person person) {
        if (person == null || person.getFirstName() == null || person.getLastName() == null) {
            throw new IllegalArgumentException("firstName and lastName are required");
        }
        String id = key(person.getFirstName(), person.getLastName());
        Person existing = people.get(id);
        if (existing == null) {
            people.put(id, person);
            return person;
        }
        existing.setAddress(person.getAddress());
        existing.setCity(person.getCity());
        existing.setZip(person.getZip());
        existing.setPhone(person.getPhone());
        existing.setEmail(person.getEmail());
        return existing;
    }
}