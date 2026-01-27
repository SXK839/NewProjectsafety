package com.safetynet.alerts.service;

import com.safetynet.alerts.model.Person;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PersonService {

    // Temporary in-memory store until DB or JSON integration
    private final Map<String, Person> people = new HashMap<>();

    private String key(String first, String last) {
        return first.toLowerCase() + "_" + last.toLowerCase();
    }

    // Add new person
    public Person addPerson(Person person) {
        people.put(key(person.getFirstName(), person.getLastName()), person);
        return person;
    }

    // Update person (first & last names cannot change)
    public Person updatePerson(Person person) {
        String id = key(person.getFirstName(), person.getLastName());

        if (!people.containsKey(id)) {
            return null;
        }

        Person existing = people.get(id);

        // Update allowed fields
        existing.setAddress(person.getAddress());
        existing.setCity(person.getCity());
        existing.setZip(person.getZip());
        existing.setPhone(person.getPhone());
        existing.setEmail(person.getEmail());

        return existing;
    }

    // Delete person
    public boolean deletePerson(String firstName, String lastName) {
        return people.remove(key(firstName, lastName)) != null;
    }
}