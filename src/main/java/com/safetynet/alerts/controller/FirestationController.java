
package com.safetynet.alerts.controller;

import com.safetynet.alerts.model.FirestationMapping;
import com.safetynet.alerts.service.AdminService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/firestation")
public class FirestationController {
	private static final Logger log = LogManager.getLogger(FirestationController.class);
	private final AdminService service;

	public FirestationController(AdminService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<?> addFirestation(@RequestBody FirestationMapping f) throws IOException {
		log.info("POST /firestation body={}", f);
		service.addFirestation(f);
		log.info("Response: {}", f);
		return ResponseEntity.ok(f);
	}

	@PutMapping
	public ResponseEntity<?> updateFirestation(@RequestBody FirestationMapping f) throws IOException {
		log.info("PUT /firestation body={}", f);
		boolean ok = service.updateFirestation(f);
		Object res = ok ? f : new java.util.HashMap<>();
		log.info("Response: {}", res);
		return ResponseEntity.ok(res);
	}

	@DeleteMapping
	public ResponseEntity<?> deleteFirestation(@RequestParam String addressOrStation) throws IOException {
		log.info("DELETE /firestation addressOrStation={}", addressOrStation);
		boolean ok = service.deleteFirestation(addressOrStation);
		Object res = ok ? java.util.Map.of("deleted", true) : new java.util.HashMap<>();
		log.info("Response: {}", res);
		return ResponseEntity.ok(res);
	}
}
