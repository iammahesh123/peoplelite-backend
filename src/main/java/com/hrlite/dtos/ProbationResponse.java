package com.hrlite.dtos;

import com.hrlite.enums.ProbationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProbationResponse {

    private UUID employeeId;
    private String employeeName;
    private String employeeCode;
    private LocalDate dateOfJoining;
    private LocalDate probationEndDate;
    private ProbationStatus probationStatus;
    private long daysRemaining;
}
