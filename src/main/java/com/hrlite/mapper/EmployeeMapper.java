package com.hrlite.mapper;

import com.hrlite.entity.Employee;
import com.hrlite.dtos.EmployeeResponse;

public final class EmployeeMapper {

    private EmployeeMapper() {}

    public static EmployeeResponse toResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .fullName(employee.getFullName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .dateOfBirth(employee.getDateOfBirth())
                .dateOfJoining(employee.getDateOfJoining())
                .employmentType(employee.getEmploymentType().name())
                .department(employee.getDepartment())
                .designation(employee.getDesignation())
                .status(employee.getStatus().name())
                .monthlyCTC(employee.getMonthlyCTC())
                .basicSalary(employee.getBasicSalary())
                .hra(employee.getHra())
                .specialAllowance(employee.getSpecialAllowance())
                .createdAt(employee.getCreatedAt())
                .build();
    }
}
