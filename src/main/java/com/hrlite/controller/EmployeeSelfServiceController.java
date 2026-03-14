package com.hrlite.controller;

import com.hrlite.dtos.ApiResponse;
import com.hrlite.entity.Employee;
import com.hrlite.entity.LetterGenerationLog;
import com.hrlite.exception.BusinessException;
import com.hrlite.exception.ErrorCodes;
import com.hrlite.repository.EmployeeRepository;
import com.hrlite.repository.LetterGenerationLogRepository;
import com.hrlite.security.UserPrincipal;
import com.hrlite.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/self-service")
@RequiredArgsConstructor
public class EmployeeSelfServiceController {

    private final EmployeeRepository employeeRepository;
    private final LetterGenerationLogRepository letterGenerationLogRepository;
    private final AuditService auditService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Employee emp = employeeRepository.findById(principal.getEmployeeId())
                .orElseThrow(() -> new BusinessException(ErrorCodes.RESOURCE_NOT_FOUND, "Employee not found"));

        Map<String, Object> profile = Map.ofEntries(
                Map.entry("id", emp.getId()),
                Map.entry("employeeCode", emp.getEmployeeCode()),
                Map.entry("firstName", emp.getFirstName()),
                Map.entry("lastName", emp.getLastName() != null ? emp.getLastName() : ""),
                Map.entry("email", emp.getEmail()),
                Map.entry("phone", emp.getPhone() != null ? emp.getPhone() : ""),
                Map.entry("dateOfBirth", emp.getDateOfBirth() != null ? emp.getDateOfBirth().toString() : ""),
                Map.entry("dateOfJoining", emp.getDateOfJoining().toString()),
                Map.entry("department", emp.getDepartment() != null ? emp.getDepartment() : ""),
                Map.entry("designation", emp.getDesignation() != null ? emp.getDesignation() : ""),
                Map.entry("employmentType", emp.getEmploymentType().name()),
                Map.entry("status", emp.getStatus().name())
        );
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<String>> updateProfile(@RequestBody Map<String, String> body) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Employee emp = employeeRepository.findById(principal.getEmployeeId())
                .orElseThrow(() -> new BusinessException(ErrorCodes.RESOURCE_NOT_FOUND, "Employee not found"));

        // Only allow updating certain fields
        if (body.containsKey("phone")) emp.setPhone(body.get("phone"));
        if (body.containsKey("email")) emp.setEmail(body.get("email"));

        employeeRepository.save(emp);
        auditService.log("UPDATE", "Employee", emp.getId().toString(), "Employee updated own profile");
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully"));
    }

    @GetMapping("/letters")
    public ResponseEntity<ApiResponse<List<LetterGenerationLog>>> getMyLetters() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<LetterGenerationLog> letters = letterGenerationLogRepository.findByEmployeeIdOrderByGeneratedAtDesc(principal.getEmployeeId());
        return ResponseEntity.ok(ApiResponse.success(letters));
    }
}
