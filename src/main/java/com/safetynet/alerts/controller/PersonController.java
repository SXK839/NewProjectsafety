
package com.safetynet.alerts.controller;

import com.safetynet.alerts.model.Person;
import com.safetynet.alerts.service.AdminService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/person")
public class PersonController {
	private static final Logger log = LogManager.getLogger(PersonController.class);
	private final AdminService service;

	public PersonController(AdminService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<?> addPerson(@RequestBody Person p) throws IOException {
		log.info("POST /person body={}", p);
		service.addPerson(p);
		log.info("Response: {}", p);
		return ResponseEntity.ok(p);
	}

	@PutMapping
	public ResponseEntity<?> updatePerson(@RequestBody Person p) throws IOException {
		log.info("PUT /person body={}", p);
		boolean ok = service.updatePerson(p);
		Object res = ok ? p : new java.util.HashMap<>();
		log.info("Response: {}", res);
		return ResponseEntity.ok(res);
	}

	@DeleteMapping
	public ResponseEntity<?> deletePerson(@RequestParam String firstName, @RequestParam String lastName)
			throws IOException {
		log.info("DELETE /person firstName={} lastName={}", firstName, lastName);
		boolean ok = service.deletePerson(firstName, lastName);
		Object res = ok ? java.util.Map.of("deleted", true) : new java.util.HashMap<>();
		log.info("Response: {}", res);
		return ResponseEntity.ok(res);
	}
}
