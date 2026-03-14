package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanRequest {
    private String name;
    private String code;
    private double price;
    private String billingCycle;
    private int employeeLimit;
    private List<String> features;
    private String description;
    private Boolean active;
}
