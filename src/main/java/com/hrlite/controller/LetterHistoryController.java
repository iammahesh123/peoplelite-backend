package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.entity.LetterGenerationLog;
import com.hrlite.service.LetterHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/letters-history")
@RequiredArgsConstructor
public class LetterHistoryController {

    private final LetterHistoryService letterHistoryService;

    @PostMapping("/log")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<LetterGenerationLog>> logGeneration(@RequestBody Map<String, String> body) {
        UUID employeeId = body.get("employeeId") != null ? UUID.fromString(body.get("employeeId")) : null;
        LetterGenerationLog log = letterHistoryService.logGeneration(
            employeeId,
            body.get("employeeName"),
            body.get("letterType"),
            body.get("templateStyle"),
            body.get("generatedBy") != null ? UUID.fromString(body.get("generatedBy")) : null
        );
        return ResponseEntity.ok(ApiResponse.success("Logged", log));
    }

    @PostMapping("/email")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<LetterGenerationLog>> emailLetter(@RequestBody Map<String, String> body) {
        UUID employeeId = body.get("employeeId") != null ? UUID.fromString(body.get("employeeId")) : null;
        LetterGenerationLog log = letterHistoryService.logAndEmail(
            employeeId,
            body.get("employeeName"),
            body.get("letterType"),
            body.get("templateStyle"),
            body.get("generatedBy") != null ? UUID.fromString(body.get("generatedBy")) : null,
            body.get("emailTo"),
            body.get("subject"),
            body.get("htmlBody")
        );
        return ResponseEntity.ok(ApiResponse.success("Letter emailed successfully", log));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<LetterGenerationLog>>> getHistory() {
        return ResponseEntity.ok(ApiResponse.success("History retrieved", letterHistoryService.getHistory()));
    }

    @GetMapping("/history/{employeeId}")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<LetterGenerationLog>>> getEmployeeHistory(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.success("History retrieved", letterHistoryService.getHistoryForEmployee(employeeId)));
    }
}
