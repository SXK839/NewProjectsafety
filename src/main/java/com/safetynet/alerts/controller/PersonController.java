package com.safetynet.alerts.controller;

import com.safetynet.alerts.model.Person;
import com.safetynet.alerts.service.PersonService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/person")
public class PersonController {

    private static final Logger log = LogManager.getLogger(PersonController.class);
    private final PersonService service;

    public PersonController(PersonService service) {
        this.service = service;
    }

    // ---------- POST /person (Add new) ----------
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addPerson(@RequestBody Person p) {
        log.info("POST /person body={}", p);
        try {
            Person created = service.addPerson(p);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) { // invalid/missing fields or body
            return ResponseEntity.badRequest()
                    .body(Map.of("code", "BAD_REQUEST", "message", e.getMessage()));
        } catch (IllegalStateException e) { // duplicate (firstName+lastName)
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("code", "CONFLICT", "message", e.getMessage()));
        }
    }

    // ---------- PUT /person?firstName&lastName (Update only) ----------
    @PutMapping(params = { "firstName", "lastName" }, consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updatePerson(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestBody Person p) {
        log.warn(">>> Entered updatePerson(RequestParam) handler");
        try {
            Person updated = service.updatePerson(firstName, lastName, p);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            if ("Person not found".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("code", "NOT_FOUND", "message", e.getMessage()));
            }
            return ResponseEntity.badRequest().body(Map.of("code", "BAD_REQUEST", "message", e.getMessage()));
        }
    }

    // ---------- DELETE /person?firstName&lastName ----------
    @DeleteMapping(params = { "firstName", "lastName" })
    public ResponseEntity<?> deletePerson(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName) {
        log.info("DELETE /person firstName={} lastName={}", firstName, lastName);
        boolean ok = service.deletePerson(firstName, lastName);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", "NOT_FOUND", "message", "Person not found"));
        }
        return ResponseEntity.noContent().build();
    }

    // ---------- GET /person?firstName&lastName ----------
    @GetMapping(params = { "firstName", "lastName" }, produces = "application/json")
    public ResponseEntity<?> getPerson(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName) {
        log.info("GET /person firstName={} lastName={}", firstName, lastName);
        var personOpt = service.getPerson(firstName, lastName);
        return personOpt.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("code", "NOT_FOUND", "message", "Person not found")));
    }

    // ---------- GET /person (no params -> 400 with guidance) ----------
    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getPersonMissingParams() {
        return ResponseEntity.badRequest().body(
                Map.of("message",
                        "Missing required query params: firstName and lastName. Try /person?firstName=John&lastName=Doe")
        );
    }

    // ---------- GET /person/all (for quick inspection) ----------
    @GetMapping("/all")
    public ResponseEntity<List<Person>> getAllPersons() {
        log.info("GET /person/all");
        List<Person> people = service.getAllPersons();
        return ResponseEntity.ok(people);
    }
}