package com.hrlite.dtos;

import com.hrlite.enums.EmploymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEmployeeRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    @Email(message = "Valid email is required")
    private String email;

    private String phone;
    private LocalDate dateOfBirth;
    private EmploymentType employmentType;
    private String department;
    private String designation;

    @DecimalMin(value = "0", message = "Monthly CTC must be non-negative")
    private BigDecimal monthlyCTC;

    @DecimalMin(value = "0", message = "Basic salary must be non-negative")
    private BigDecimal basicSalary;

    @DecimalMin(value = "0", message = "HRA must be non-negative")
    private BigDecimal hra;

    @DecimalMin(value = "0", message = "Special allowance must be non-negative")
    private BigDecimal specialAllowance;
}
