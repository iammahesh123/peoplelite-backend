package com.hrlite.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfferLetterResponse {
    private UUID id;
    private String candidateName;
    private String candidateEmail;
    private UUID templateId;
    private String templateName;
    private String variablesJson;
    private LocalDateTime generatedAt;
}
