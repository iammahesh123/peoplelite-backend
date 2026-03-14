package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlanInfoResponse {
    private String planName;
    private int maxEmployees;
    private long currentEmployeeCount;
    private List<String> availableFeatures;
}
