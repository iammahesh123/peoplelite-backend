package com.hrlite.controller;

import com.hrlite.exception.ResourceNotFoundException;
import com.hrlite.dtos.ApiResponse;
import com.hrlite.security.UserPrincipal;
import com.hrlite.entity.Department;
import com.hrlite.repository.DepartmentRepository;
import com.hrlite.dtos.BulkDepartmentRequest;
import com.hrlite.dtos.DepartmentRequest;
import com.hrlite.dtos.DepartmentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<DepartmentResponse> departments = departmentRepository.findByTenantIdAndActiveTrue(principal.getTenantId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @PostMapping
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DepartmentRequest request) {

        if (departmentRepository.existsByTenantIdAndName(principal.getTenantId(), request.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.success("Department already exists",
                            mapToResponse(departmentRepository.findByTenantIdAndName(principal.getTenantId(), request.getName()).get())));
        }

        Department department = Department.builder()
                .tenantId(principal.getTenantId())
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Department saved = departmentRepository.save(department);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Department created", mapToResponse(saved)));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> createBulkDepartments(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BulkDepartmentRequest request) {

        List<DepartmentResponse> results = new ArrayList<>();
        for (String name : request.getNames()) {
            if (!departmentRepository.existsByTenantIdAndName(principal.getTenantId(), name)) {
                Department department = Department.builder()
                        .tenantId(principal.getTenantId())
                        .name(name)
                        .build();
                Department saved = departmentRepository.save(department);
                results.add(mapToResponse(saved));
            } else {
                departmentRepository.findByTenantIdAndName(principal.getTenantId(), name)
                        .ifPresent(d -> results.add(mapToResponse(d)));
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Departments created", results));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id, @Valid @RequestBody DepartmentRequest request) {
        Department department = departmentRepository.findByTenantIdAndId(principal.getTenantId(), id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));

        if (request.getName() != null) department.setName(request.getName());
        if (request.getDescription() != null) department.setDescription(request.getDescription());
        department.setUpdatedAt(LocalDateTime.now());

        Department saved = departmentRepository.save(department);
        return ResponseEntity.ok(ApiResponse.success("Department updated", mapToResponse(saved)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<ApiResponse<String>> deleteDepartment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        Department department = departmentRepository.findByTenantIdAndId(principal.getTenantId(), id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        department.setActive(false);
        department.setUpdatedAt(LocalDateTime.now());
        departmentRepository.save(department);
        return ResponseEntity.ok(ApiResponse.success("Department deactivated"));
    }

    private DepartmentResponse mapToResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .active(department.isActive())
                .createdAt(department.getCreatedAt())
                .build();
    }
}
