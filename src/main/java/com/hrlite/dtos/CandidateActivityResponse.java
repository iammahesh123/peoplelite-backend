package com.hrlite.dtos;

import com.hrlite.enums.CandidateActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateActivityResponse {

    private UUID id;
    private UUID candidateId;
    private CandidateActivityType activityType;
    private String description;
    private UUID performedBy;
    private String performedByName;
    private LocalDateTime createdAt;
}
