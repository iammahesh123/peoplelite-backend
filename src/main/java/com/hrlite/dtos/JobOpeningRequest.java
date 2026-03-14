package com.hrlite.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobOpeningRequest {

    @NotBlank(message = "Job title is required")
    private String title;

    private String department;
    private String location;
    private String employmentType;
    private Integer experienceMin;
    private Integer experienceMax;
    private String description;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
}
