package com.hrlite.service;

import com.hrlite.dtos.ProbationResponse;
import com.hrlite.dtos.ProbationUpdateRequest;
import com.hrlite.entity.Employee;
import com.hrlite.entity.TenantContext;
import com.hrlite.enums.EmployeeStatus;
import com.hrlite.enums.ProbationStatus;
import com.hrlite.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProbationService {

    private final EmployeeRepository employeeRepository;

    public List<ProbationResponse> getEmployeesOnProbation() {
        UUID tenantId = TenantContext.getCurrentTenant();
        return employeeRepository.findByTenantId(tenantId).stream()
                .filter(e -> e.getProbationStatus() == ProbationStatus.ON_PROBATION)
                .map(this::mapToResponse)
                .toList();
    }

    public List<ProbationResponse> getUpcomingProbationEnds(int daysFromNow) {
        UUID tenantId = TenantContext.getCurrentTenant();
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysFromNow);

        return employeeRepository.findByTenantId(tenantId).stream()
                .filter(e -> e.getProbationStatus() == ProbationStatus.ON_PROBATION &&
                        e.getProbationEndDate() != null &&
                        !e.getProbationEndDate().isBefore(today) &&
                        !e.getProbationEndDate().isAfter(endDate))
                .map(this::mapToResponse)
                .toList();
    }

    public void confirmEmployee(UUID employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        employee.setProbationStatus(ProbationStatus.CONFIRMED);
        employee.setProbationEndDate(null);
        employeeRepository.save(employee);
    }

    public void extendProbation(UUID employeeId, ProbationUpdateRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        employee.setProbationStatus(ProbationStatus.EXTENDED);
        employee.setProbationEndDate(request.getNewProbationEndDate());
        employeeRepository.save(employee);
    }

    private ProbationResponse mapToResponse(Employee employee) {
        LocalDate today = LocalDate.now();
        long daysRemaining = 0;
        if (employee.getProbationEndDate() != null) {
            daysRemaining = ChronoUnit.DAYS.between(today, employee.getProbationEndDate());
        }

        return ProbationResponse.builder()
                .employeeId(employee.getId())
                .employeeName(employee.getFullName())
                .employeeCode(employee.getEmployeeCode())
                .dateOfJoining(employee.getDateOfJoining())
                .probationEndDate(employee.getProbationEndDate())
                .probationStatus(employee.getProbationStatus())
                .daysRemaining(Math.max(0, daysRemaining))
                .build();
    }
}
