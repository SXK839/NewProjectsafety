package com.safetynet.alerts.controller;


import com.safetynet.alerts.model.MedicalRecord;
import com.safetynet.alerts.service.AdminService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/medicalRecord")
public class MedicalRecordController {

    private static final Logger log = LogManager.getLogger(MedicalRecordController.class);
    private final AdminService service;

    public MedicalRecordController(AdminService service) {
        this.service = service;
    }

    // ---------- POST /medicalRecord (Add) ----------
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> add(@RequestBody MedicalRecord m) throws IOException {
        log.info("POST /medicalRecord body={}", m);
        if (m == null || m.getFirstName() == null || m.getLastName() == null
                || m.getFirstName().isBlank() || m.getLastName().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("code", "BAD_REQUEST", "message", "firstName and lastName are required"));
        }
        // Duplicate check
        if (service.getMedicalRecord(m.getFirstName(), m.getLastName()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("code", "CONFLICT", "message", "Medical record already exists for given firstName and lastName"));
        }
        service.addMedicalRecord(m);
        return ResponseEntity.status(HttpStatus.CREATED).body(m);
    }

    // ---------- PUT /medicalRecord?firstName&lastName (Update only; names immutable) ----------
    @PutMapping(params = { "firstName", "lastName" }, consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> update(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName")  String lastName,
            @RequestBody MedicalRecord body) throws IOException {
        log.info("PUT /medicalRecord firstName={} lastName={} body={}", firstName, lastName, body);

        if (body == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("code", "BAD_REQUEST", "message", "Request body is missing or invalid JSON"));
        }

        // Enforce immutable identity
        body.setFirstName(firstName);
        body.setLastName(lastName);

        boolean ok = service.updateMedicalRecord(body);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", "NOT_FOUND", "message", "Medical record not found"));
        }
        return ResponseEntity.ok(body);
    }

    // ---------- DELETE /medicalRecord?firstName&lastName ----------
    @DeleteMapping(params = { "firstName", "lastName" })
    public ResponseEntity<?> delete(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName")  String lastName) throws IOException {
        log.info("DELETE /medicalRecord firstName={} lastName={}", firstName, lastName);
        boolean removed = service.deleteMedicalRecord(firstName, lastName);
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", "NOT_FOUND", "message", "Medical record not found"));
        }
        return ResponseEntity.noContent().build();
    }

    // ---------- Optional GETs for verification ----------
    @GetMapping(params = { "firstName", "lastName" }, produces = "application/json")
    public ResponseEntity<?> getOne(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName")  String lastName) {
        var opt = service.getMedicalRecord(firstName, lastName);
        return opt.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("code", "NOT_FOUND", "message", "Medical record not found")));
    }

    @GetMapping(value = "/all", produces = "application/json")
    public ResponseEntity<List<MedicalRecord>> getAll() {
        return ResponseEntity.ok(service.getAllMedicalRecords());
    }
}