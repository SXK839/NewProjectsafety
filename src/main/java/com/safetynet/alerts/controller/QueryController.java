
package com.safetynet.alerts.controller;

import com.safetynet.alerts.service.AlertService;
import com.safetynet.alerts.repository.DataRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class QueryController {
	private static final Logger log = LogManager.getLogger(QueryController.class);
	private final AlertService service;
	private final DataRepository repo;

	public QueryController(AlertService s, DataRepository r) throws Exception {
		this.service = s;
		this.repo = r;
		this.repo.load();
	}

	@GetMapping("/firestation")
	public ResponseEntity<?> firestation(@RequestParam("stationNumber") int n) {
		log.info("GET /firestation?stationNumber={}", n);
		Object res = service.firestation(n);
		log.info("Response: {}", res);
		return ResponseEntity.ok(res);
	}

	@GetMapping("/childAlert")
	public ResponseEntity<?> childAlert(@RequestParam("address") String a) {
		log.info("GET /childAlert?address={}", a);
		Object res = service.childAlert(a);
		log.info("Response: {}", res);
		return ResponseEntity.ok(res);
	}

	@GetMapping("/phoneAlert")
	public ResponseEntity<?> phoneAlert(@RequestParam("firestation") int s) {
		log.info("GET /phoneAlert?firestation={}", s);
		Object res = service.phoneAlert(s);
		log.info("Response: {}", res);
		return ResponseEntity.ok(res);
	}

	@GetMapping("/fire")
	public ResponseEntity<?> fire(@RequestParam("address") String a) {
		log.info("GET /fire?address={}", a);
		Object res = service.fire(a);
		log.info("Response: {}", res);
		return ResponseEntity.ok(res);
	}

	@GetMapping("/flood/stations")
	public ResponseEntity<?> flood(@RequestParam("stations") String csv) {
		log.info("GET /flood/stations?stations={}", csv);
		List<Integer> s = Arrays.stream(csv.split(",")).map(String::trim).filter(v -> !v.isEmpty())
				.map(Integer::parseInt).collect(Collectors.toList());
		Object res = service.floodStations(s);
		log.info("Response: {}", res);
		return ResponseEntity.ok(res);
	}

	@GetMapping("/personInfo")
	public ResponseEntity<?> personInfo(@RequestParam("lastName") String l) {
		log.info("GET /personInfo?lastName={}", l);
		Object res = service.personInfo(l);
		log.info("Response: {}", res);
		return ResponseEntity.ok(res);
	}

	@GetMapping("/communityEmail")
	public ResponseEntity<?> communityEmail(@RequestParam("city") String c) {
		log.info("GET /communityEmail?city={}", c);
		Object res = service.communityEmail(c);
		log.info("Response: {}", res);
		return ResponseEntity.ok(res);
	}
}
