package com.safetynet.alerts.controller;

import com.safetynet.alerts.dto.PersonInfoDTO;
import com.safetynet.alerts.service.PersonInfoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class PersonInfoController {

    private static final Logger log = LogManager.getLogger(PersonInfoController.class);
    private final PersonInfoService service;

    public PersonInfoController(PersonInfoService service) {
        this.service = service;
    }

    // GET /personInfo?lastName=Boyd
    @GetMapping(value = "/personInfo", params = { "lastName" }, produces = "application/json")
    public ResponseEntity<?> getPersonInfoByLastName(@RequestParam("lastName") String lastName) {
        log.info("GET /personInfo lastName={}", lastName);

        if (lastName == null || lastName.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("code", "BAD_REQUEST", "message", "Query parameter 'lastName' is required"));
        }

        List<PersonInfoDTO> results = service.getPersonInfoByLastName(lastName);

        if (results == null || results.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("code", "NOT_FOUND", "message", "No persons found with lastName=" + lastName));
        }

        return ResponseEntity.ok(results);
    }
}
