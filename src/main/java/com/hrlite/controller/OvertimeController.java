package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.dtos.OvertimeLogRequest;
import com.hrlite.entity.OvertimeLog;
import com.hrlite.security.UserPrincipal;
import com.hrlite.service.OvertimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/overtime")
@RequiredArgsConstructor
public class OvertimeController {

    private final OvertimeService overtimeService;

    @PostMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<OvertimeLog>> logOvertime(
            @Valid @RequestBody OvertimeLogRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        OvertimeLog log = overtimeService.logOvertime(
                request.getEmployeeId(), request.getDate(),
                request.getHours(), request.getReason(), principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Overtime logged", log));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<OvertimeLog>> approveOvertime(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Overtime approved",
                overtimeService.approveOvertime(id, principal)));
    }

    @GetMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<OvertimeLog>>> getAllOvertimeLogs() {
        return ResponseEntity.ok(ApiResponse.success(overtimeService.getAllOvertimeLogs()));
    }

    @GetMapping("/month")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<OvertimeLog>>> getOvertimeByMonth(
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success(overtimeService.getOvertimeLogs(month, year)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<OvertimeLog>>> getMyOvertime(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                overtimeService.getMyOvertimeLogs(principal.getEmployeeId())));
    }
}
