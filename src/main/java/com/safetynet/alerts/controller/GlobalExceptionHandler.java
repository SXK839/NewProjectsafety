package com.safetynet.alerts.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

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
        // Change from numeric to string enum name to satisfy $.code == "BAD_REQUEST"
        m.put("code", status.name()); // e.g., BAD_REQUEST, INTERNAL_SERVER_ERROR
        return m;
    }

    /**
     * Triggered when an endpoint declares @RequestParam(required = true) and it is not provided.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> missingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        log.error("Missing request parameter: {}", ex.getMessage(), ex);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String msg = "Required request parameter is missing: " + ex.getParameterName();
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body(status, msg, req.getRequestURI()));
    }

    /**
     * Triggered when @GetMapping(..., params="lastName") is hit without that query param.
     */
    @ExceptionHandler(UnsatisfiedServletRequestParameterException.class)
    public ResponseEntity<?> unsatisfiedParams(UnsatisfiedServletRequestParameterException ex, HttpServletRequest req) {
        log.error("Parameter conditions not met: {}", ex.getMessage(), ex);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String msg = "Required request parameter is missing or conditions not met: " + ex.getMessage();
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body(status, msg, req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> typeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        log.error("Type mismatch: {}", ex.getMessage(), ex);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String msg = "Invalid parameter type";
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body(status, msg, req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        log.error("Validation error: {}", ex.getMessage(), ex);
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body(status, "Validation failed", req.getRequestURI()));
    }

    @ExceptionHandler(java.io.IOException.class)
    public ResponseEntity<?> io(java.io.IOException ex, HttpServletRequest req) {
        log.error("I/O error: {}", ex.getMessage(), ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body(status, "Storage error", req.getRequestURI()));
    }

    /**
     * Keep generic handler LAST so it doesn’t swallow more specific 4xx mappings.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> generic(Exception ex, HttpServletRequest req) {
        log.error("Unhandled error: {}", ex.getMessage(), ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body(status, "Unexpected server error", req.getRequestURI()));
    }
}