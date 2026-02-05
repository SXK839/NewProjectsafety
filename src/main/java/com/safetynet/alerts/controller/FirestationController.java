package com.safetynet.alerts.controller;

import com.safetynet.alerts.model.FirestationMapping;
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
@RequestMapping("/firestation")
public class FirestationController {

    private static final Logger log = LogManager.getLogger(FirestationController.class);
    private final AdminService service;

    public FirestationController(AdminService service) {
        this.service = service;
    }

    // ---------- POST /firestation (Add mapping) ----------
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> add(@RequestBody FirestationMapping mapping) throws IOException {
        log.info("POST /firestation body={}", mapping);
        try {
            // AdminService.addFirestation throws on IO; repository should throw on duplicate or we can allow repo to decide.
            service.addFirestation(mapping);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapping);
        } catch (IllegalArgumentException e) {
            // For invalid body (e.g., missing address or station)
            return ResponseEntity.badRequest().body(Map.of("code", "BAD_REQUEST", "message", e.getMessage()));
        } catch (IllegalStateException e) {
            // For duplicate address mapping (if repo throws)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("code", "CONFLICT", "message", e.getMessage()));
        }
    }

    // ---------- PUT /firestation?address=... (Update station-for-address) ----------
    @PutMapping(params = { "address" }, consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateStation(
            @RequestParam("address") String address,
            @RequestBody FirestationMapping body) throws IOException {
        log.info("PUT /firestation address={} body={}", address, body);
        if (body == null) {
            return ResponseEntity.badRequest().body(Map.of("code", "BAD_REQUEST", "message", "Request body is missing or invalid JSON"));
        }
        // Build mapping for AdminService: address is the selector, station comes from body
        FirestationMapping patch = new FirestationMapping(address, body.getStation());
        boolean ok = service.updateFirestation(patch);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", "NOT_FOUND", "message", "Mapping not found"));
        }
        return ResponseEntity.ok(patch);
    }

    // ---------- DELETE /firestation?address=... (Delete one address) ----------
    @DeleteMapping(params = { "address" })
    public ResponseEntity<?> deleteByAddress(@RequestParam("address") String address) throws IOException {
        log.info("DELETE /firestation address={}", address);
        boolean ok = service.deleteFirestation(address);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", "NOT_FOUND", "message", "Mapping not found"));
        }
        return ResponseEntity.noContent().build();
    }

    // ---------- DELETE /firestation?station=... (Delete all for station) ----------
    @DeleteMapping(params = { "station" })
    public ResponseEntity<?> deleteByStation(@RequestParam("station") int station) throws IOException {
        log.info("DELETE /firestation station={}", station);
        // New AdminService method (see patch below)
        int count = service.deleteFirestationByStation(station);
        if (count == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", "NOT_FOUND", "message", "No mappings found for the given station"));
        }
        return ResponseEntity.noContent().build();
    }

 // ---------- GET /firestation?address=... ----------
    @GetMapping(params = { "address" }, produces = "application/json")
    public ResponseEntity<?> getByAddress(@RequestParam("address") String address) {
        var opt = service.getFirestationByAddress(address);
        return opt.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("code", "NOT_FOUND", "message", "Mapping not found")));
    }

    // ---------- GET /firestation/all ----------
    @GetMapping(value = "/all", produces = "application/json")
    public ResponseEntity<List<FirestationMapping>> getAll() {
        var all = service.getAllFirestations();
        return ResponseEntity.ok(all);  // <-- 200 OK with array (may be empty)
    }
    
    
}