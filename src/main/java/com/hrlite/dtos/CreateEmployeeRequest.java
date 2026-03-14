package com.hrlite.dtos;

import com.hrlite.enums.EmploymentType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateEmployeeRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    private String phone;

    private LocalDate dateOfBirth;

    @NotNull(message = "Date of joining is required")
    private LocalDate dateOfJoining;

    @NotNull(message = "Employment type is required")
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

    private String password;
}
