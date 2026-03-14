package com.hrlite.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateOfferLetterRequest {

    @NotNull(message = "Template ID is required")
    private UUID templateId;

    @NotBlank(message = "Candidate name is required")
    private String candidateName;

    private String candidateEmail;

    private UUID employeeId;

    private Map<String, String> variables;
}
