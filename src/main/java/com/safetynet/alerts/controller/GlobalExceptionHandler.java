
package com.safetynet.alerts.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.*;

@ControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger log = LogManager.getLogger(GlobalExceptionHandler.class);

	private Map<String, Object> body(HttpStatus status, String message, String path) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("timestamp", OffsetDateTime.now().toString());
		m.put("status", status.value());
		m.put("error", status.getReasonPhrase());
		m.put("message", message);
		m.put("path", path);
		return m;
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<?> typeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
		log.error("Type mismatch: {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(body(HttpStatus.BAD_REQUEST, "Invalid parameter type", req.getRequestURI()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<?> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
		log.error("Validation error: {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(body(HttpStatus.BAD_REQUEST, "Validation failed", req.getRequestURI()));
	}

	@ExceptionHandler(java.io.IOException.class)
	public ResponseEntity<?> io(java.io.IOException ex, HttpServletRequest req) {
		log.error("I/O error: {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(body(HttpStatus.INTERNAL_SERVER_ERROR, "Storage error", req.getRequestURI()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> generic(Exception ex, HttpServletRequest req) {
		log.error("Unhandled error: {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(body(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", req.getRequestURI()));
	}
}
