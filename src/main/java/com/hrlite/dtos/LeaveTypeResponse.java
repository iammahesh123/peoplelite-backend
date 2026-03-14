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
public class LeaveTypeResponse {
    private UUID id;
    private String name;
    private String code;
    private int defaultBalance;
    private boolean paid;
    private boolean active;
}
