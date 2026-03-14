package com.hrlite.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateJoiningLetterRequest {

    @NotNull(message = "Template ID is required")
    private UUID templateId;

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    private Map<String, String> variables;
}
