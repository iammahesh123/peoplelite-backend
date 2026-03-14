package com.hrlite.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health check endpoint for monitoring and deployment health checks.
 * Used by Docker healthchecks and load balancers.
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    /**
     * Health check endpoint that returns the service status.
     *
     * @return Map containing status and service information
     */
    @GetMapping
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "hr-lite-backend");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    /**
     * Readiness probe endpoint for Kubernetes or other orchestration platforms.
     *
     * @return Map containing readiness status
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> ready() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "READY");
        response.put("service", "hr-lite-backend");
        return ResponseEntity.ok(response);
    }

    /**
     * Liveness probe endpoint for Kubernetes or other orchestration platforms.
     *
     * @return Map containing liveness status
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, String>> live() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ALIVE");
        response.put("service", "hr-lite-backend");
        return ResponseEntity.ok(response);
    }
}
