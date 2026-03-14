package com.hrlite.dtos;

import com.hrlite.enums.EmployeeAvailabilityStatus;
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
public class TeamAvailabilityResponse {

    private UUID employeeId;
    private String employeeName;
    private String employeeCode;
    private String department;
    private LocalDate date;
    private EmployeeAvailabilityStatus status;
    private String leaveType;
}
