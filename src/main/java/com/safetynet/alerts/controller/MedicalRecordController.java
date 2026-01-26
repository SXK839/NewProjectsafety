
package com.safetynet.alerts.controller;

import com.safetynet.alerts.model.MedicalRecord;
import com.safetynet.alerts.service.AdminService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/medicalRecord")
public class MedicalRecordController {

	private static final Logger log = LogManager.getLogger(MedicalRecordController.class);
	private final AdminService service;

	public MedicalRecordController(AdminService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<?> addMedicalRecord(@RequestBody MedicalRecord m) throws IOException {
		log.info("POST /medicalRecord body={}", m);
		service.addMedicalRecord(m);
		log.info("Response: {}", m);
		return ResponseEntity.ok(m);
	}

	@PutMapping
	public ResponseEntity<?> updateMedicalRecord(@RequestBody MedicalRecord m) throws IOException {
		log.info("PUT /medicalRecord body={}", m);
		boolean ok = service.updateMedicalRecord(m);
		Object res = ok ? m : new java.util.HashMap<>();
		log.info("Response: {}", res);
		return ResponseEntity.ok(res);
	}

	@DeleteMapping
	public ResponseEntity<?> deleteMedicalRecord(@RequestParam String firstName, @RequestParam String lastName)
			throws IOException {
		log.info("DELETE /medicalRecord firstName={} lastName={}", firstName, lastName);
		boolean ok = service.deleteMedicalRecord(firstName, lastName);
		Object res = ok ? java.util.Map.of("deleted", true) : new java.util.HashMap<>();
		log.info("Response: {}", res);
		return ResponseEntity.ok(res);
	}
}
