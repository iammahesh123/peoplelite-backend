package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.entity.AttendanceRecord;
import com.hrlite.security.UserPrincipal;
import com.hrlite.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<AttendanceRecord>> checkIn(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Checked in", attendanceService.checkIn(principal)));
    }

    @PostMapping("/check-out")
    public ResponseEntity<ApiResponse<AttendanceRecord>> checkOut(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success("Checked out", attendanceService.checkOut(principal)));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<AttendanceRecord>> getTodayRecord(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getTodayRecord(principal)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<AttendanceRecord>>> getMyRecords(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getMyRecords(principal, month, year)));
    }

    @GetMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<AttendanceRecord>>> getAllRecords(
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success(attendanceService.getAllRecords(month, year)));
    }
}
