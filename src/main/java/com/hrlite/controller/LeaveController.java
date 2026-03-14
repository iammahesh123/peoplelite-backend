package com.hrlite.controller;

import com.hrlite.dtos.*;
import com.hrlite.security.UserPrincipal;
import com.hrlite.service.LeaveService;
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
@RequestMapping("/api/v1/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    // --- Leave Requests ---

    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaveRequestResponse>>> getLeaveRequests(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<LeaveRequestResponse> leaves;
        if ("FOUNDER".equals(principal.getRole())) {
            leaves = leaveService.getAllLeaveRequests();
        } else {
            leaves = leaveService.getMyLeaveRequests(principal.getEmployeeId());
        }
        return ResponseEntity.ok(ApiResponse.success(leaves));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> applyLeave(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ApplyLeaveRequest request) {
        LeaveRequestResponse response = leaveService.applyLeave(principal.getEmployeeId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave applied", response));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> approveLeave(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) LeaveActionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Leave approved",
                leaveService.approveLeave(id, principal, request)));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> rejectLeave(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) LeaveActionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Leave rejected",
                leaveService.rejectLeave(id, principal, request)));
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<List<LeaveBalanceResponse>>> getMyBalances(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getMyBalances(principal.getEmployeeId())));
    }

    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<List<LeaveRequestResponse>>> getLeaveCalendar(
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getLeaveCalendar(month, year)));
    }

    // --- Leave Type Settings ---

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<LeaveTypeResponse>>> getLeaveTypes() {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getLeaveTypes()));
    }

    @PostMapping("/types")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<LeaveTypeResponse>> createLeaveType(
            @Valid @RequestBody LeaveTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave type created", leaveService.createLeaveType(request)));
    }

    @PutMapping("/types/{id}")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<LeaveTypeResponse>> updateLeaveType(
            @PathVariable UUID id, @Valid @RequestBody LeaveTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Leave type updated",
                leaveService.updateLeaveType(id, request)));
    }

    @DeleteMapping("/types/{id}")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<Void>> deleteLeaveType(@PathVariable UUID id) {
        leaveService.deleteLeaveType(id);
        return ResponseEntity.ok(ApiResponse.success("Leave type deactivated"));
    }
}
