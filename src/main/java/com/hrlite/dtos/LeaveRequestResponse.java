package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaveRequestResponse {
    private UUID id;
    private UUID employeeId;
    private String employeeName;
    private UUID leaveTypeId;
    private String leaveTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private double days;
    private String reason;
    private String status;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
}
