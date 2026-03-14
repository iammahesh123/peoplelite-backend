package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaveBalanceResponse {
    private UUID leaveTypeId;
    private String leaveTypeName;
    private String leaveTypeCode;
    private double total;
    private double used;
    private double remaining;
    private int year;
}
