package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.dtos.ReimbursementRequest;
import com.hrlite.entity.Reimbursement;
import com.hrlite.security.UserPrincipal;
import com.hrlite.service.ReimbursementService;
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
@RequestMapping("/api/v1/reimbursements")
@RequiredArgsConstructor
public class ReimbursementController {

    private final ReimbursementService reimbursementService;

    @PostMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<Reimbursement>> createReimbursement(
            @Valid @RequestBody ReimbursementRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        Reimbursement reimbursement = reimbursementService.create(
                request.getEmployeeId(),
                request.getCategory(),
                request.getAmount(),
                request.getDescription(),
                request.getMonth(),
                request.getYear(),
                principal
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reimbursement created", reimbursement));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<Reimbursement>> approveReimbursement(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {

        Reimbursement reimbursement = reimbursementService.approve(id, principal);
        return ResponseEntity.ok(ApiResponse.success("Reimbursement approved", reimbursement));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<Reimbursement>> rejectReimbursement(@PathVariable UUID id) {
        Reimbursement reimbursement = reimbursementService.reject(id);
        return ResponseEntity.ok(ApiResponse.success("Reimbursement rejected", reimbursement));
    }

    @GetMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<Reimbursement>>> getAllReimbursements() {
        return ResponseEntity.ok(ApiResponse.success(reimbursementService.getAll()));
    }

    @GetMapping("/month")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<Reimbursement>>> getReimbursementsByMonth(
            @RequestParam int month, @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.success(reimbursementService.getByMonth(month, year)));
    }
}